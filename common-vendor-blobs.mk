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

VENDOR_FOLDER := vendor/motorola/omap4-common

# Backup Tool and audio
PRODUCT_COPY_FILES += \
    $(VENDOR_FOLDER)/proprietary/audio/alsa.omap4.so:/system/lib/hw/alsa.omap4.so \
    $(VENDOR_FOLDER)/proprietary/audio/libaudio_ext.so:/system/lib/libaudio_ext.so \
    $(VENDOR_FOLDER)/prebuilt/bin/50-backup.sh:system/addon.d/50-backup.sh \
    $(VENDOR_FOLDER)/prebuilt/bin/backuptool.functions:system/bin/backuptool.functions \
    $(VENDOR_FOLDER)/prebuilt/bin/backuptool.sh:system/bin/backuptool.sh \
    $(VENDOR_FOLDER)/prebuilt/bin/backuptool.sh:system/bin/blacklist
