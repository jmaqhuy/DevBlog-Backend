package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.request.ShareExternalPostRequest;
import com.example.devblogbackend.entity.ExternalPost;
import com.example.devblogbackend.repository.ExternalPostRepository;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ExternalPostService {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;

    @Value("${external-post.timeout:15000}")
    private int timeoutMs;

    @Value("${external-post.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36}")
    private String userAgent;

    @Value("${external-post.enable-images:false}")
    private boolean enableImages;

    private static final List<String> BLOCKED_RESOURCES = Arrays.asList(
            "font", "stylesheet", "media", "websocket", "manifest", "other"
    );

    private static final List<String> OG_IMAGE_SELECTORS = Arrays.asList(
            "meta[property='og:image']",
            "meta[property='og:image:url']",
            "meta[name='twitter:image']",
            "meta[name='twitter:image:src']",
            "meta[itemprop='image']"
    );

    private static final List<String> TITLE_SELECTORS = Arrays.asList(
            "meta[property='og:title']",
            "meta[name='twitter:title']",
            "meta[itemprop='name']",
            "title"
    );

    private static final List<String> SITE_NAME_SELECTORS = Arrays.asList(
            "meta[property='og:site_name']",
            "meta[name='application-name']",
            "meta[name='apple-mobile-web-app-title']"
    );

    public ExternalPost addExternalPost(ShareExternalPostRequest request) {
        String normalizedUrl = normalizeUrl(request.getUrl());
        log.info("Processing external post for URL: {}", normalizedUrl);

        Page page = null;
        try {
            URI uri = new URI(normalizedUrl);
            ExternalPost externalPost = new ExternalPost();
            externalPost.setDomain(uri.getHost());
            externalPost.setPath(uri.getPath());

            page = createOptimizedPage();

            // Navigate with retry mechanism
            navigateWithRetry(page, normalizedUrl);

            // Wait for dynamic content to load
            waitForContent(page);

            // Extract metadata
            extractMetadata(page, externalPost, uri);

            log.info("Successfully extracted metadata for: {}", normalizedUrl);
            return externalPost;

        } catch (Exception e) {
            log.error("Error processing URL: {}", normalizedUrl, e);
            throw new RuntimeException("Failed to extract metadata from URL: " + normalizedUrl, e);
        } finally {
            closePage(page);
        }
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            URI uri = new URI(url);
            return uri.normalize().toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + url, e);
        }
    }

    private void navigateWithRetry(Page page, String url) {
        int maxRetries = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(timeoutMs));
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("Navigation attempt {} failed for URL: {}", attempt, url, e);

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Navigation interrupted", ie);
                    }
                }
            }
        }

        throw new RuntimeException("Failed to navigate after " + maxRetries + " attempts", lastException);
    }

    private void waitForContent(Page page) {
        try {
            // Wait for common meta tags to be present
            page.waitForFunction("() => " +
                            "document.querySelector('meta[property=\"og:title\"]') || " +
                            "document.querySelector('title') || " +
                            "document.readyState === 'complete'",
                    new Page.WaitForFunctionOptions().setTimeout(5000));
        } catch (Exception e) {
            log.debug("Timeout waiting for content to load, proceeding with extraction");
        }
    }

    private void extractMetadata(Page page, ExternalPost externalPost, URI baseUri) {
        // Extract title
        String title = extractBestMatch(page, TITLE_SELECTORS);
        externalPost.setTitle(cleanText(title));

        // Extract site name
        String siteName = extractBestMatch(page, SITE_NAME_SELECTORS);
        if (!StringUtils.hasText(siteName)) {
            siteName = baseUri.getHost();
        }
        externalPost.setSiteName(cleanText(siteName));

        // Extract thumbnail
        String thumbnail = extractBestMatch(page, OG_IMAGE_SELECTORS);
        externalPost.setThumbnail(resolveUrl(thumbnail, baseUri));

        // Extract favicon
        String favicon = extractFavicon(page, baseUri);
        externalPost.setWebLogo(favicon);
    }

    private String extractBestMatch(Page page, List<String> selectors) {
        return selectors.stream()
                .map(selector -> {
                    try {
                        var element = page.querySelector(selector);
                        if (element != null) {
                            String content = element.getAttribute("content");
                            if (content != null && !content.trim().isEmpty()) {
                                return content.trim();
                            }
                            // For title tag, get text content
                            if (selector.equals("title")) {
                                return element.textContent();
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error extracting from selector {}: {}", selector, e.getMessage());
                    }
                    return null;
                })
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private String extractFavicon(Page page, URI baseUri) {
        // Try to find favicon with size preference (larger icons first)
        String[] faviconSelectors = {
                "link[rel='apple-touch-icon'][sizes*='180']",
                "link[rel='apple-touch-icon'][sizes*='152']",
                "link[rel='apple-touch-icon']",
                "link[rel='icon'][sizes*='32']",
                "link[rel='icon'][sizes*='16']",
                "link[rel='icon']",
                "link[rel='shortcut icon']"
        };

        for (String selector : faviconSelectors) {
            try {
                var element = page.querySelector(selector);
                if (element != null) {
                    String href = element.getAttribute("href");
                    if (StringUtils.hasText(href)) {
                        return resolveUrl(href, baseUri);
                    }
                }
            } catch (Exception e) {
                log.debug("Error extracting favicon with selector {}: {}", selector, e.getMessage());
            }
        }

        return baseUri.getScheme() + "://" + baseUri.getHost() + "/favicon.ico";
    }

    private String resolveUrl(String url, URI baseUri) {
        if (!StringUtils.hasText(url)) {
            return null;
        }

        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url;
            }

            if (url.startsWith("//")) {
                return baseUri.getScheme() + ":" + url;
            }

            if (url.startsWith("/")) {
                return baseUri.getScheme() + "://" + baseUri.getHost() + url;
            }

            // Relative URL
            String basePath = baseUri.getPath();
            if (basePath.endsWith("/")) {
                return baseUri.getScheme() + "://" + baseUri.getHost() + basePath + url;
            } else {
                int lastSlash = basePath.lastIndexOf('/');
                String parentPath = lastSlash > 0 ? basePath.substring(0, lastSlash + 1) : "/";
                return baseUri.getScheme() + "://" + baseUri.getHost() + parentPath + url;
            }

        } catch (Exception e) {
            log.warn("Failed to resolve URL: {} against base: {}", url, baseUri, e);
            return url;
        }
    }

    private String cleanText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    @PostConstruct
    public void init() {
        log.info("Initializing ExternalPostService with Playwright");
        playwright = Playwright.create();

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(Arrays.asList(
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-web-security",
                        "--disable-features=VizDisplayCompositor"
                )));

        // Create a persistent context for better performance
        context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(userAgent)
                .setViewportSize(1920, 1080)
                .setJavaScriptEnabled(true)
                .setBypassCSP(true));
    }

    private Page createOptimizedPage() {
        Page page = context.newPage();

        // Block unnecessary resources for faster loading
        page.route("**/*", route -> {
            String resourceType = route.request().resourceType();

            if (!enableImages && "image".equals(resourceType)) {
                route.abort();
                return;
            }

            if (BLOCKED_RESOURCES.contains(resourceType)) {
                route.abort();
                return;
            }

            route.resume();
        });

        // Set reasonable timeouts
        page.setDefaultTimeout(timeoutMs);
        page.setDefaultNavigationTimeout(timeoutMs);

        return page;
    }

    private void closePage(Page page) {
        if (page != null) {
            try {
                page.close();
            } catch (Exception e) {
                log.warn("Error closing page: {}", e.getMessage());
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up ExternalPostService resources");

        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            log.warn("Error closing browser context: {}", e.getMessage());
        }

        try {
            if (browser != null) {
                browser.close();
            }
        } catch (Exception e) {
            log.warn("Error closing browser: {}", e.getMessage());
        }

        try {
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            log.warn("Error closing playwright: {}", e.getMessage());
        }
    }
}