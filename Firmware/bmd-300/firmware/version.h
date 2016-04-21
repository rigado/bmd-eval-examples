#ifndef _VERSION_H_
#define _VERSION_H_

#define FIRMWARE_MAJOR_VERSION      1
#define FIRMWARE_MINOR_VERSION      0
#define FIRMWARE_BUILD_NUMBER       0

#define BUILD_VERSION_NUMBER        1
#define API_VERSION_NUMBER          2

#define _V_STR( x ) #x
#define _VERSION_TO_STRING( major, minor, build, build_version ) _V_STR(major) \
            "." _V_STR(minor) "." _V_STR(build) " (" _V_STR(build_version) ")"
#define _HW_VERSION_TO_STRING( major, minor ) _V_STR(major) "." _V_STR(minor)

#define FIRMWARE_VERSION_STRING \
    _VERSION_TO_STRING( FIRMWARE_MAJOR_VERSION, FIRMWARE_MINOR_VERSION, \
    FIRMWARE_BUILD_NUMBER, BUILD_VERSION_NUMBER )

#define HARDWARE_MAJOR_VERSION      1
#define HARDWARE_MINOR_VERSION      0

#define HARDWARE_VERSION_STRING \
    _HW_VERSION_TO_STRING( HARDWARE_MAJOR_VERSION, HARDWARE_MINOR_VERSION )
#endif
