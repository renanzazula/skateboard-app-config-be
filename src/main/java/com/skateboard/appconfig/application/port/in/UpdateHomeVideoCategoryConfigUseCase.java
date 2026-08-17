package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;

import java.util.Set;

public interface UpdateHomeVideoCategoryConfigUseCase {

    record Command(String adminId, HomeVideoCategoryConfig.Mode mode, Set<String> enabledCategoryIds) {}

    HomeVideoCategoryConfig execute(Command command);
}
