package com.gospelee.api.service;

import com.gospelee.api.dto.applink.AppLinkDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AppLinkService {

  AppLinkDTO makeAppUrl(HttpServletRequest request, String userAgent);

  String makeRedirectHtml();
}
