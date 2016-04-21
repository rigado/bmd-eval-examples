/** @file ble_bmdeval_handler.h
*
* @brief This module handles incoming write events from the ble_bmdeval_t
*        service and manages data reads from the accelerometer.
*
* @par
* COPYRIGHT NOTICE: (c) Rigado
* All rights reserved. 
*
* Source code licensed under Software License Agreement in license.txt.
* You should have received a copy with purchase of BMD series product 
* and with this repository.  If not, contact modules@rigado.com.
*/

#ifndef __BLE_BMDEVAL_HANDLER_H__
#define __BLE_BMDEVAL_HANDLER_H__

#include <stdint.h>
#include "ble_bmdeval.h"

#ifdef __cpluscplus
extern "C" {
#endif
    
uint32_t ble_bmdeval_handler_init(ble_bmdeval_init_t * const p_ble_bmdeval_init,
                                    ble_bmdeval_t * const p_bmdeval);
    
#ifdef __cplusplus
}
#endif

#endif
