package com.tecnrt.modules.hod.dto;

import java.util.List;

public record BatchProvisionRequest(
        List<String> rollNumbers
) {}

