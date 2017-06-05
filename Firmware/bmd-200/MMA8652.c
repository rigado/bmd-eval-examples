/** @file
 * 
 * @brief Module for assisting in I2C communications with the MMA8652 accelerometer.
 *
 * @details This module is capable of performing chip specific operations such as setup. This module does not
 * implement every feature of the MMA8652, but does provide a framework to implement additional functionality.
 * For more details on custom setup parameters, see the datasheet for the MMA8652. 
 */

#include "MMA8652.h"
#include "I2Cdrv.h"
#include "app_timer.h"

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

#define STANDBY_MASK								 0x01				/*used to set standby state */

bool MMA8652Init(void)
{
    uint8_t accel_reg_buff = 0;
   
    
    // Reset all registers to POR values
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG2_ADDR, 0x40);
    do{
        // wait for the reset bit to clear
        I2CReadReg(MMA8652_ADDR, MMA8652_CTRL_REG2_ADDR, &accel_reg_buff, 1);
    }while(accel_reg_buff & 0x40);
		
		/* For Testing: Use I2C to read WHO_AM_I ID Register from accel */
    /* First send register address */
    I2CReadReg(MMA8652_ADDR, MMA8652_ID_REG_ADDR, &accel_reg_buff, 1);
    if(accel_reg_buff != MMA8652_ID_VAL)
    {
        return false;
    }
    
    /* Setup the control registers */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xA8);            /* REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG2_ADDR, 0x18);            /* REG 0x2B Setup for low-power, no auto-sleep */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG3_ADDR, 0x00);            /* REG 0x2C No interrupts wake device, active low int, push-pull */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG4_ADDR, 0x00);            /* REG 0x2D */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG5_ADDR, 0xFD);            /* REG 0x2E All interrupt sources to INT1 */
    
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x11);     /* REG 0x0E HPF / scale +/-2,4,8g */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x00); /* REG 0x0F HPF settings */
    
    return true;
}
void MMA8652Deinit(void)
{
	uint8_t accel_reg_buff = 0;
	I2CReadReg(MMA8652_ADDR, MMA8652_CTRL_REG2_ADDR, &accel_reg_buff, 1);
	accel_reg_buff &= ~(1 << STANDBY_MASK); //This will clear the least significant bit in the register (ACTIVE)
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, accel_reg_buff);            /* REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz */
}

void MMA8652EnableVibrationMode()
{
    uint8_t motion_thsx;
    uint8_t motion_debounce_count;
        
    motion_thsx = CONFIG1_VIB_MOTION_THSX_VAL;
    motion_debounce_count = CONFIG1_VIB_MOTION_DBNC_VAL;			
        
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0x18);                              /* REG 0x2A Set to Standby */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x10);                       /* REG 0x0E Enable HPF */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x03);                   /* REG 0x0F Configure HPF */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_TRANSIENT_CFG_REG_ADDR, 0x1E);                      /* REG 0x1D Enable XZY motion detection */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_TRANSIENT_THS_REG_ADDR, motion_thsx);               /* REG 0x1F Set motion threshold */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_TRANSIENT_COUNT_REG_ADDR, motion_debounce_count);   /* REG 0x20 Set debounce count */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG4_ADDR, 0x20);                              /* REG 0x2D Enable transient interrupt */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG5_ADDR, 0x20);                              /* REG 0x2E Set INT to INT1 */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0x83);                              /* REG 0x2A Data Rate : 6.25Hz, Normal Read, Active mode */     
}

void MMA8652EnableOrientationMode(void)
{       
    // Initialize register to get acceleration data
		I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xA8);             	/* STANDBY MODE. REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Active mode */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x00);       /* REG 0x0E HPF / scale +/-2,4,8g - Disable HPF */
    
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PL_CFG_REG_ADDR, 0xC0);             /* REG 0x11 Portrait/Landscape Configuration Reg: Enable P/L detection */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PL_COUNT_REG_ADDR, 0x0A);           /* REG 0x12 Portrait/Landscape Debounce Register */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PL_BF_ZCOMP_REG_ADDR, 0x07);        /* REG 0x13 Back/Front and Z Compensation register */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_P_L_THS_REG_ADDR, 0x91);            /* REG 0x14 Portrait/Landscape Threshold and Hysteresis register (75 degrees) */
    
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG2_ADDR, 0x03);              /* REG 0x2B Setup for low power mode, no auto-sleep */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG4_ADDR, 0x10);              /* REG 0x2D Enable Orientation interrupt */
		I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xAB);              /* REG 0x2A 0xA1 = ASLP_RATE: 6.25hz Data Rate: 12.5Hz, Normal Read, Active mode */
}


void MMA845EnableDataReadyMode(void)
{   
		I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xA8);              /* STANDBY MODE. REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Active mode */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x11);       /* REG 0x0E HPF / scale +/-2,4,8g - Enable HPF */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x10);   /* REG 0x0F HPF settings - HPF for pulse on, 0.25Hz cutoff for 12.5 rate */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x02);       /* REG 0x0E HPF / scale +/-2,4,8g - Disable HPF */
    I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG4_ADDR, 0x01);              /* REG 0x2D Enable data ready interrupt */
    
		I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xAB);              /* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Fast mode, Active mode */
		//I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xA1);            /* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 50hz, Normal Read, Active mode */
    //I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0x99);            /* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 100hz, Normal Read, Active mode */
    //I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0x91);            /* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 200hz, Normal Read, Active mode */
    //I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0x89);            /* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 400hz, Normal Read, Active mode */
    //I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0x81);            /* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 800hz, Normal Read, Active mode */
}

void MMA845EnableDoubleTapMode(void)
{
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xA8);              /* STANDBY MODE. REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Active mode */
  I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x12);       /* REG 0x0E HPF / scale +/-2,4,8g - Enable HPF */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x00);   /* REG 0x0F HPF settings - HPF for pulse on, 0.25Hz cutoff for 12.5 rate */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PULSE_CFG_REG_ADDR, 0x6A);   				/* REG 0x21 Pulse Config, Doubletap on any axis */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PULSE_THSX_REG_ADDR, 0x1F);   			/* REG 0x23 Pulse Threshold for X axis, half-range */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PULSE_THSY_REG_ADDR, 0x1F);   			/* REG 0x24 Pulse Threshold for Y axis, half-range */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PULSE_THSZ_REG_ADDR, 0x1F);   			/* REG 0x25 Pulse Threshold for Z axis, half-range */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PULSE_TMLT_REG_ADDR, 0x10);   			/* REG 0x26 Pulse Time limit */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PULSE_LTCY_REG_ADDR, 0x04);   			/* REG 0x27 Pulse Latency limit */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_PULSE_WIND_REG_ADDR, 0x1F);   			/* REG 0x28 Pulse Window limit */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG4_ADDR, 0x08);              /* REG 0x2D Enable pulse interrupt */
    
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xAB); 							/* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Fast mode, Active mode */
}

void MMA845EnableMotionMode(void)
{
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xA8);              /* STANDBY MODE. REG 0x2A 0xA8 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Active mode */
  I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_XYZ_DATA_CFG_REG_ADDR, 0x10);       /* REG 0x0E HPF / scale +/-2,4,8g - Enable HPF */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_HP_FILTER_CUTOFF_REG_ADDR, 0x10);   /* REG 0x0F HPF settings - HPF for pulse on, 0.25Hz cutoff for 12.5 rate */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_FF_MT_CFG_REG_ADDR, 0xF8);    			/* REG 0x15 Motion/Freefall config register. Motion on any axis */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_FF_MT_THR_REG_ADDR, 0x12);    			/* REG 0x17 Motion/Freefall threshold register. half-range */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_FF_MT_COUNT_REG_ADDR, 0x04);    		/* REG 0x18 Motion/Freefall debounce register. 8 counts */
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG4_ADDR, 0x04);              /* REG 0x2D Enable motion/freefall interrupt */
	
	I2CWriteReg(MMA8652_WRITE_ADDR, MMA8652_CTRL_REG1_ADDR, 0xAB); 							/* REG 0x2A 0x98 = ASLP_RATE: 6.25hz Data Rate: 12.5hz, Normal Read, Fast mode, Active mode */    
}
