/** @file
 * 
 * @brief Module for assisting in I2C communications with the MMA8652 accelerometer.
 *
 * @details This module is capable of performing chip specific operations such as setup. This module does not
 * implement every feature of the MMA8652, but does provide a framework to implement additional functionality.
 * For more details on custom setup parameters, see the datasheet for the MMA8652. 
 */
#include <string.h>

#include "nrf_delay.h"
#include "MMA8652.h"
#include "app_timer.h"
#include "app_util_platform.h"
#include "nrf_drv_twi.h"

#define MAX_PENDING_TRANSACTIONS    5

// Button Configuration 
// Config1 (RED) parameters  
#define CONFIG1_BUTTON_MOTION_THSX_VAL  8       /* motion threshold (.5g) */
#define CONFIG1_BUTTON_MOTION_DBNC_VAL  4       /* debounce time */
// Config2 (BLUE) parameters  
#define CONFIG2_BUTTON_MOTION_THSX_VAL  16      /* motion threshold (1g) */
#define CONFIG2_BUTTON_MOTION_DBNC_VAL  3       /* debounce time */
// Config3 (GREEN) parameters  
#define CONFIG3_BUTTON_MOTION_THSX_VAL  24      /* motion threshold (1.5g) */
#define CONFIG3_BUTTON_MOTION_DBNC_VAL  2       /* debounce time */
// Config4 (YELLOW) parameters */
#define CONFIG4_BUTTON_MOTION_THSX_VAL  32      /* motion threshold (2g) */
#define CONFIG4_BUTTON_MOTION_DBNC_VAL  2       /* debounce time */

// Vibration Configuration
// Config1 (RED) parameters  
#define CONFIG1_VIB_MOTION_THSX_VAL  1          /* motion threshold () */
#define CONFIG1_VIB_MOTION_DBNC_VAL  2          /* debounce time */
// Config2 (BLUE) parameters  
#define CONFIG2_VIB_MOTION_THSX_VAL  3          /* motion threshold () */
#define CONFIG2_VIB_MOTION_DBNC_VAL  3          /* debounce time */
// Config3 (GREEN) parameters  
#define CONFIG3_VIB_MOTION_THSX_VAL  4          /* motion threshold () */
#define CONFIG3_VIB_MOTION_DBNC_VAL  3          /* debounce time */
// Config4 (YELLOW) parameters */
#define CONFIG4_VIB_MOTION_THSX_VAL  5          /* motion threshold () */
#define CONFIG4_VIB_MOTION_DBNC_VAL  5          /* debounce time */

#define STANDBY_BIT_POS				 0x00	    /*used to set standby state */

static const nrf_drv_twi_t m_twi_bus = NRF_DRV_TWI_INSTANCE(0);

static uint32_t read_reg(uint8_t reg, uint8_t * const p_data, uint8_t len);
static uint32_t write_reg(uint8_t reg, uint8_t data);

bool MMA8652Init(void)
{
    uint8_t accel_reg_buff = 0;
    
    const nrf_drv_twi_config_t m_twi_bus_config = NRF_DRV_TWI_DEFAULT_CONFIG(0);
    nrf_drv_twi_init(&m_twi_bus, &m_twi_bus_config, NULL, NULL);
    nrf_drv_twi_enable(&m_twi_bus);
    
    MMA8652Deinit();
    
    // Reset all registers to POR values
    write_reg(MMA8652_CTRL_REG2_ADDR, 0x40);
    
    nrf_delay_us(250);
    
    do
    {
        // wait for the reset bit to clear
        uint32_t err = read_reg(MMA8652_CTRL_REG2_ADDR, &accel_reg_buff, 1);
        if(err != NRF_SUCCESS)
        {
            return false;
        }
    } while(accel_reg_buff & 0x40);
		
	/* Verify WHO_AM_I ID Register from accel */
    read_reg(MMA8652_ID_REG_ADDR, &accel_reg_buff, 1);
    if(accel_reg_buff != MMA8652_ID_VAL)
    {
        return false;
    }
    
    /* Setup the control registers */
    write_reg(MMA8652_CTRL_REG1_ADDR, 0xAA);
    write_reg(MMA8652_CTRL_REG2_ADDR, 0x00);            /* REG 0x2B Setup for low-power, no auto-sleep */
    write_reg(MMA8652_CTRL_REG3_ADDR, 0x00);            /* REG 0x2C No interrupts wake device, active low int, push-pull */
    write_reg(MMA8652_CTRL_REG4_ADDR, 0x00);            /* REG 0x2D */
    write_reg(MMA8652_CTRL_REG5_ADDR, 0xFD);            /* REG 0x2E All interrupt sources to INT1 */
    
    write_reg(MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x02);     /* REG 0x0E HPF / scale +/-2,4,8g */
    write_reg(MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x00); /* REG 0x0F HPF settings */
    
    uint8_t reg = 0;
    read_reg(MMA8652_CTRL_REG1_ADDR, &reg, 1);
    
    return true;
}

void MMA8652Deinit(void)
{
	uint8_t accel_reg_buff = 0;
	read_reg(MMA8652_CTRL_REG1_ADDR, &accel_reg_buff, 1);
    
	//This will clear the least significant bit in the register (ACTIVE)
    accel_reg_buff &= ~(1 << STANDBY_BIT_POS); 
    
    /* REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz */
	write_reg(MMA8652_CTRL_REG1_ADDR, accel_reg_buff);            
}

void MMA8652EnableVibrationMode()
{
    uint8_t motion_thsx;
    uint8_t motion_debounce_count;
        
    motion_thsx = CONFIG1_VIB_MOTION_THSX_VAL;
    motion_debounce_count = CONFIG1_VIB_MOTION_DBNC_VAL;			
        
    write_reg(MMA8652_CTRL_REG1_ADDR, 0x18);                              /* REG 0x2A Set to Standby */
    write_reg(MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x10);                       /* REG 0x0E Enable HPF */
    write_reg(MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x03);                   /* REG 0x0F Configure HPF */
    write_reg(MMA8652_TRANSIENT_CFG_REG_ADDR, 0x1E);                      /* REG 0x1D Enable XZY motion detection */
    write_reg(MMA8652_TRANSIENT_THS_REG_ADDR, motion_thsx);               /* REG 0x1F Set motion threshold */
    write_reg(MMA8652_TRANSIENT_COUNT_REG_ADDR, motion_debounce_count);   /* REG 0x20 Set debounce count */
    write_reg(MMA8652_CTRL_REG4_ADDR, 0x20);                              /* REG 0x2D Enable transient interrupt */
    write_reg(MMA8652_CTRL_REG5_ADDR, 0x20);                              /* REG 0x2E Set INT to INT1 */
    write_reg(MMA8652_CTRL_REG1_ADDR, 0x83);                              /* REG 0x2A Data Rate : 6.25Hz, Normal Read, Active mode */     
}

void MMA8652EnableOrientationMode(void)
{       
    // Initialize register to get acceleration data
	write_reg(MMA8652_CTRL_REG1_ADDR, 0xA8);         /* STANDBY MODE. REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Active mode */
    write_reg(MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x00);  /* REG 0x0E HPF / scale +/-2,4,8g - Disable HPF */
    
    write_reg(MMA8652_PL_CFG_REG_ADDR, 0xC0);        /* REG 0x11 Portrait/Landscape Configuration Reg: Enable P/L detection */
    write_reg(MMA8652_PL_COUNT_REG_ADDR, 0x0A);      /* REG 0x12 Portrait/Landscape Debounce Register */
    write_reg(MMA8652_PL_BF_ZCOMP_REG_ADDR, 0x07);   /* REG 0x13 Back/Front and Z Compensation register */
    write_reg(MMA8652_P_L_THS_REG_ADDR, 0x91);       /* REG 0x14 Portrait/Landscape Threshold and Hysteresis register (75 degrees) */
    
    write_reg(MMA8652_CTRL_REG2_ADDR, 0x03);         /* REG 0x2B Setup for low power mode, no auto-sleep */
    write_reg(MMA8652_CTRL_REG4_ADDR, 0x10);         /* REG 0x2D Enable Orientation interrupt */
	write_reg(MMA8652_CTRL_REG1_ADDR, 0xAB);         /* REG 0x2A 0xA1 = ASLP_RATE: 6.25hz Data Rate: 12.5Hz, Normal Read, Active mode */
}


uint32_t MMA8652EnableDataReadyMode(bool state)
{   
    uint8_t ctrl_reg_1;
    
    // Disable active mode
    uint32_t err = read_reg(MMA8652_CTRL_REG1_ADDR, &ctrl_reg_1, sizeof(ctrl_reg_1));
    if(err != NRF_SUCCESS)
    {
        return err;
    }
    
    ctrl_reg_1 &= ~(0x01);
	err = write_reg(MMA8652_CTRL_REG1_ADDR, ctrl_reg_1); 
    if(err != NRF_SUCCESS)
    {
        return err;
    }
    
    if(state)
    {
        err = write_reg(MMA8652_CTRL_REG4_ADDR, 0x01);              /* REG 0x2D Enable data ready interrupt */
        if(err != NRF_SUCCESS)
        {
            return err;
        }
        
        //Re-enable active mode
        ctrl_reg_1 |= (0x01);
        err = write_reg(MMA8652_CTRL_REG1_ADDR, ctrl_reg_1);
    }
    
    return err;
}

void MMA8652EnableDoubleTapMode(void)
{
	write_reg(MMA8652_CTRL_REG1_ADDR, 0xA8);            /* STANDBY MODE. REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Active mode */
    write_reg(MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x12);     /* REG 0x0E HPF / scale +/-2,4,8g - Enable HPF */
	write_reg(MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x00); /* REG 0x0F HPF settings - HPF for pulse on, 0.25Hz cutoff for 12.5 rate */
	write_reg(MMA8652_PULSE_CFG_REG_ADDR, 0x6A);        /* REG 0x21 Pulse Config, Doubletap on any axis */
	write_reg(MMA8652_PULSE_THSX_REG_ADDR, 0x1F);   	/* REG 0x23 Pulse Threshold for X axis, half-range */
	write_reg(MMA8652_PULSE_THSY_REG_ADDR, 0x1F);   	/* REG 0x24 Pulse Threshold for Y axis, half-range */
	write_reg(MMA8652_PULSE_THSZ_REG_ADDR, 0x1F);   	/* REG 0x25 Pulse Threshold for Z axis, half-range */
	write_reg(MMA8652_PULSE_TMLT_REG_ADDR, 0x10);   	/* REG 0x26 Pulse Time limit */
	write_reg(MMA8652_PULSE_LTCY_REG_ADDR, 0x04);   	/* REG 0x27 Pulse Latency limit */
	write_reg(MMA8652_PULSE_WIND_REG_ADDR, 0x1F);   	/* REG 0x28 Pulse Window limit */
	write_reg(MMA8652_CTRL_REG4_ADDR, 0x08);            /* REG 0x2D Enable pulse interrupt */
    
	write_reg( MMA8652_CTRL_REG1_ADDR, 0xAB); 			/* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Fast mode, Active mode */
}

void MMA8652EnableMotionMode(void)
{
	write_reg(MMA8652_CTRL_REG1_ADDR, 0xA8);            /* STANDBY MODE. REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Active mode */
    write_reg(MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x10);     /* REG 0x0E HPF / scale +/-2,4,8g - Enable HPF */
	write_reg(MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x10); /* REG 0x0F HPF settings - HPF for pulse on, 0.25Hz cutoff for 12.5 rate */
	write_reg(MMA8652_FF_MT_CFG_REG_ADDR, 0xF8);    	/* REG 0x15 Motion/Freefall config register. Motion on any axis */
	write_reg(MMA8652_FF_MT_THR_REG_ADDR, 0x12);    	/* REG 0x17 Motion/Freefall threshold register. half-range */
	write_reg(MMA8652_FF_MT_COUNT_REG_ADDR, 0x04);    	/* REG 0x18 Motion/Freefall debounce register. 8 counts */
	write_reg(MMA8652_CTRL_REG4_ADDR, 0x04);            /* REG 0x2D Enable motion/freefall interrupt */
	
	write_reg(MMA8652_CTRL_REG1_ADDR, 0xAB); 			/* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Fast mode, Active mode */    
}

void MMA8652ReadAllAxisData(int8_t * const p_accel_data)
{
    uint8_t data[4];
    memset(data, 0, sizeof(data));

    /* Read X, Y, Z data buffers */
    read_reg(MMA8652_DATA_STATUS_REG_ADDR, data, sizeof(data));        
    
    memcpy(p_accel_data, &data[1], sizeof(data) - 1);
}

void MMA8652ReadReg(uint8_t reg, uint8_t * const p_reg_val, uint8_t len)
{
    read_reg(reg, p_reg_val, len);
}

static uint32_t write_reg(uint8_t reg, uint8_t data)
{
    uint8_t bus_data[2] = { reg, data };
    return nrf_drv_twi_tx(&m_twi_bus, MMA8652_ADDR, bus_data, sizeof(bus_data), false);
}

static uint32_t read_reg(uint8_t reg, uint8_t * p_data, uint8_t len)
{
    nrf_drv_twi_xfer_desc_t tx_xfer_desc = 
        NRF_DRV_TWI_XFER_DESC_TX(MMA8652_ADDR, &reg, 1);
    nrf_drv_twi_xfer_desc_t rx_xfer_desc = 
        NRF_DRV_TWI_XFER_DESC_RX(MMA8652_ADDR, p_data, len);
    uint32_t err = nrf_drv_twi_xfer(&m_twi_bus, &tx_xfer_desc, 
        (NRF_DRV_TWI_FLAG_NO_XFER_EVT_HANDLER | NRF_DRV_TWI_FLAG_TX_NO_STOP));   
    if(err != NRF_SUCCESS)
    {
        return err;
    }
    
    err = nrf_drv_twi_xfer(&m_twi_bus, &rx_xfer_desc, 
        NRF_DRV_TWI_FLAG_NO_XFER_EVT_HANDLER);
    return err;
}
