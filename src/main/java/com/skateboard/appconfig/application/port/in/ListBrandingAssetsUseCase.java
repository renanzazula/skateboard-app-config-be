package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.BrandingAsset;

import java.util.List;

public interface ListBrandingAssetsUseCase {
    List<BrandingAsset> execute();
}
