# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Pick up overlay for features that depend on non-open-source files
#DEVICE_PACKAGE_OVERLAYS := vendor/motorola/common/overlay

PRODUCT_PACKAGES := \
    librs_jni \
    VideoEditor \
    VoiceDialer \
    Basic \
    HoloSpiralWallpaper \
    MagicSmokeWallpapers \
    NoiseField \
    Galaxy4 \
    LiveWallpapers \
    LiveWallpapersPicker \
    VisualizationWallpapers \
    PhaseBeam \
    OTAUpdateCenter

# Publish that we support the live wallpaper feature.
PRODUCT_COPY_FILES := \
    packages/wallpapers/LivePicker/android.software.live_wallpaper.xml:/system/etc/permissions/android.software.live_wallpaper.xml

ifeq ($(BOARD_USES_KEXEC),true)
    TYPE := KEXEC
else
    TYPE := STOCK
endif
OTATIME := $(shell date +%Y%m%d-%H%M)
UTC := $(shell date -u +%Y%m%d)
FLAVOR := $(shell echo $(TARGET_PRODUCT) | cut -f1 -d '_')
DEVICE := $(shell echo $(TARGET_PRODUCT) | cut -f2 -d '_')
PRODUCT_PROPERTY_OVERRIDES += \
    otaupdater.otatime=$(OTATIME) \
    otaupdater.sdcard.os=sdcard-ext \
    otaupdater.sdcard.recovery=sdcard-ext \
    otaupdater.otaid=$(TYPE)-JB-$(TARGET_PRODUCT)
ifeq ($(FLAVOR),cm)
    PRODUCT_PROPERTY_OVERRIDES += otaupdater.otaver=$(UTC)-UNOFFICIAL-$(DEVICE)
endif
ifeq ($(FLAVOR),aokp)
    DATE = $(shell vendor/aokp/tools/getdate)
    PRODUCT_PROPERTY_OVERRIDES += otaupdater.otaver=$(DATE)
endif
ifeq ($(FLAVOR),cna)
    PRODUCT_PROPERTY_OVERRIDES += otaupdater.otaver=$(UTC)
endif
ifeq ($(FLAVOR),full)
    PRODUCT_PROPERTY_OVERRIDES += otaupdater.otaver=$(UTC)
endif

$(call inherit-product, vendor/motorola/common/common-vendor-blobs.mk)
$(call inherit-product, vendor/motorola/common/common_drm_phone.mk)
