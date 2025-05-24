package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.request.ShareExternalPostRequest;
import com.example.devblogbackend.entity.ExternalPost;
import com.example.devblogbackend.repository.ExternalPostRepository;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class ExternalPostService {
    private Playwright playwright;
    private Browser browser;

    public ExternalPost addExternalPost(ShareExternalPostRequest request) {
        ExternalPost externalPost = new ExternalPost();
        Page page = null;
        try {
            String url = request.getUrl();
            // get domain & path
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            URL parsedUrl = new URL(url);
            externalPost.setDomain(parsedUrl.getHost());
            externalPost.setPath(parsedUrl.getPath());


            // get logo, title, thumbnail
            page = createNewPage();
            page.route("**/*.{png,jpg,jpeg,gif,svg,css,js}", route -> route.abort());

            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(10000));

            String title = page.title();

            String thumbnail = null;
            if (page.querySelector("meta[property='og:image']") != null) {
                thumbnail = page.querySelector("meta[property='og:image']").getAttribute("content");

            } else if (page.querySelector("meta[name='twitter:image']") != null) {
                thumbnail = page.querySelector("meta[name='twitter:image']").getAttribute("content");
            }
            if (thumbnail != null && !thumbnail.isEmpty()) {
                if (!thumbnail.startsWith("http")) {
                    if (!thumbnail.startsWith("/")) {
                        thumbnail = "/" + thumbnail;
                    }
                    thumbnail = "https://" + externalPost.getDomain() + thumbnail;
                }
            }

            String siteName = null;
            if (page.querySelector("meta[property='og:site_name']") != null) {
                siteName = page.querySelector("meta[property='og:site_name']").getAttribute("content");
            }

            String logo = null;
            if (page.querySelector("link[rel='icon']") != null) {
                logo = page.querySelector("link[rel='icon']").getAttribute("href");
                if (!logo.contains(externalPost.getDomain())) {
                    if (!logo.startsWith("/")){
                        logo = "/" + logo;
                    }
                    logo = "https://" + externalPost.getDomain() + logo;
                }
            } else if (page.querySelector("link[rel='shortcut icon']") != null) {
                logo = page.querySelector("link[rel='shortcut icon']").getAttribute("href");
            }
            externalPost.setSiteName(siteName);
            externalPost.setTitle(title);
            externalPost.setThumbnail(thumbnail);
            externalPost.setWebLogo(logo);
            return externalPost;
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing URL:", e);
        }
        finally {
            if (page != null) {
                page.close();
            }
        }
    }

    @PostConstruct
    public void init() {
        playwright = Playwright.create();
        BrowserType chromium = playwright.chromium();
        browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    public Page createNewPage() {
        // Tạo page mới cho mỗi request
        return browser.newPage(new Browser.NewPageOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"));
    }

    @PreDestroy
    public void close() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
