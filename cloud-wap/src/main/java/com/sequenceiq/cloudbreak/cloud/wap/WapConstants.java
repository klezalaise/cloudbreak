package com.sequenceiq.cloudbreak.cloud.wap;

import static com.sequenceiq.cloudbreak.common.type.CloudConstants.WAP;


import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;

public class WapConstants {

    public static final Platform WAP_PLATFORM = Platform.platform(WAP);
    public static final Variant WAP_VARIANT = Variant.variant(WAP);

    private WapConstants() {
    }
}
