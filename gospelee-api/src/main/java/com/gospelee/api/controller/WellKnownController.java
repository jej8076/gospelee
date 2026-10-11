package com.gospelee.api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
public class WellKnownController {

  @GetMapping(value = "/apple-app-site-association", produces = MediaType.APPLICATION_JSON_VALUE)
  public String appleAppSiteAssociation() {
    return """
        {
          "applinks": {
            "apps": [],
            "details": [
              {
                "appID": "W28XUX376U.org.podo",
                "paths": ["/app-link/*", "/api/journal-share/*"]
              }
            ]
          }
        }
        """;
  }

  @GetMapping(value = "/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
  public String assetLinks() {
    return """
        [{
          "relation": ["delegate_permission/common.handle_all_urls"],
          "target": {
            "namespace": "android_app",
            "package_name": "org.podo",
            "sha256_cert_fingerprints": [
              "66:F6:17:3C:64:E2:ED:25:A2:00:F4:43:55:00:12:B6:CA:9F:15:45:7F:BB:CD:97:F2:F7:A3:B9:7D:81:16:22"
            ]
          }
        }]
        """;
  }
}
