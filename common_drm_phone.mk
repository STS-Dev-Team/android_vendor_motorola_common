VENDOR_FOLDER := vendor/motorola/common

# Blobs necessary for drm
PRODUCT_COPY_FILES +=  \
    $(VENDOR_FOLDER)/proprietary/common_drm/com.google.widevine.software.drm.xml:system/etc/permissions/com.google.widevine.software.drm.xml \
    $(VENDOR_FOLDER)/proprietary/common_drm/com.google.widevine.software.drm.jar:system/framework/com.google.widevine.software.drm.jar \
    $(VENDOR_FOLDER)/proprietary/common_drm/phone/lib/libfrsdk.so:system/lib/libfrsdk.so \
    $(VENDOR_FOLDER)/proprietary/common_drm/phone/lib/libWVphoneAPI.so:system/lib/libWVphoneAPI.so \
    $(VENDOR_FOLDER)/proprietary/common_drm/phone/vendor/lib/libwvdrm_L1.so:system/vendor/lib/libwvdrm_L1.so \
    $(VENDOR_FOLDER)/proprietary/common_drm/phone/vendor/lib/libwvm.so:system/vendor/lib/libwvm.so \
    $(VENDOR_FOLDER)/proprietary/common_drm/phone/vendor/lib/libWVStreamControlAPI_L1.so:system/vendor/lib/libWVStreamControlAPI_L1.so \
    $(VENDOR_FOLDER)/proprietary/common_drm/phone/vendor/lib/drm/libdrmwvmplugin.so:system/vendor/lib/drm/libdrmwvmplugin.so

PRODUCT_PROPERTY_OVERRIDES += \
    drm.service.enabled=true

