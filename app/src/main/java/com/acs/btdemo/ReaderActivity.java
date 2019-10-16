/*
 * Copyright (C) 2014-2018 Advanced Card Systems Ltd. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Advanced
 * Card Systems Ltd. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ACS.
 */
package com.acs.btdemo;

import java.io.UnsupportedEncodingException;

import de.hdodenhof.circleimageview.CircleImageView;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr1255uj1Reader.OnBatteryLevelAvailableListener;
import com.acs.bluetooth.Acr1255uj1Reader.OnBatteryLevelChangeListener;
import com.acs.bluetooth.Acr3901us1Reader;
import com.acs.bluetooth.Acr3901us1Reader.OnBatteryStatusAvailableListener;
import com.acs.bluetooth.Acr3901us1Reader.OnBatteryStatusChangeListener;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReader.OnAtrAvailableListener;
import com.acs.bluetooth.BluetoothReader.OnAuthenticationCompleteListener;
import com.acs.bluetooth.BluetoothReader.OnCardPowerOffCompleteListener;
import com.acs.bluetooth.BluetoothReader.OnCardStatusAvailableListener;
import com.acs.bluetooth.BluetoothReader.OnCardStatusChangeListener;
import com.acs.bluetooth.BluetoothReader.OnDeviceInfoAvailableListener;
import com.acs.bluetooth.BluetoothReader.OnEnableNotificationCompleteListener;
import com.acs.bluetooth.BluetoothReader.OnEscapeResponseAvailableListener;
import com.acs.bluetooth.BluetoothReader.OnResponseApduAvailableListener;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderGattCallback.OnConnectionStateChangeListener;
import com.acs.bluetooth.BluetoothReaderManager;
import com.acs.bluetooth.BluetoothReaderManager.OnReaderDetectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class  ReaderActivity extends AppCompatActivity implements
        TxPowerDialogFragment.TxPowerDialogListener {
    public static final String TAG = ReaderActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    /* Default master key. */
    private static final String DEFAULT_3901_MASTER_KEY = "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF";
    /* Get 8 bytes random number APDU. */
    private static final String DEFAULT_3901_APDU_COMMAND = "80 84 00 00 08";
    /* Get Serial Number command (0x02) escape command. */
    private static final String DEFAULT_3901_ESCAPE_COMMAND = "02";

    /* Default master key. */
    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";

    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    private static final String DEFAULT_1255_APDU_COMMAND = "FF B0 00 04 01";
    /* Get firmware version escape command. */
    private static final String DEFAULT_1255_ESCAPE_COMMAND = "E0 00 00 18 00";

    private static final byte[] AUTO_POLLING_START = { (byte) 0xE0, 0x00, 0x00, 0x40, 0x01 };
    private static final byte[] AUTO_POLLING_STOP = { (byte) 0xE0, 0x00, 0x00,
            0x40, 0x00 };

    /* String keys for save/restore instance state. */
    private static final String ATR_STRING = "atr";
    private static final String AUTHENTICATION_KEY = "authenticatinKey";
    private static final String APDU_COMMAND = "apduCommand";
    private static final String RESPONSE_APDU = "responseApdu";
    private static final String ESCAPE_COMMAND = "escapeCommand";
    private static final String ESCAPE_RESPONSE = "escapeResponse";

    private static final String DEVICE_INFO_SYSTEM_ID = "systemId";
    private static final String DEVICE_INFO_MODEL_NUM_STRING = "modelNum";
    private static final String DEVICE_INFO_SERIAL_NUM_STRING = "serialNum";
    private static final String DEVICE_INFO_FW_REV_STRING = "fwRev";
    private static final String DEVICE_INFO_HW_REV_STRING = "hwRev";
    private static final String DEVICE_INFO_MANUFACTURER_NAME_STRING = "manufacturerName";

    private static final String BATTERY_LEVEL_STRING = "batteryLv";
    private static final String BATTERY_LEVEL_2_STRING = "batteryLv2";
    private static final String BATTERY_STATUS_STRING = "batteryStatus";
    private static final String BATTERY_STATUS_2_STRING = "batteryStatus2";
    private static final String SLOT_STATUS_STRING = "cardStatus";
    private static final String SLOT_STATUS_2_STRING = "cardStatus2";

    /* Reader to be connected. */
    private String mDeviceName;
    private String mDeviceAddress;
    private int mConnectState = BluetoothReader.STATE_DISCONNECTED;

    /* UI control */
    private Button mClear;
    private Button mAuthentication;
    private Button mStartPolling;
    private Button mStopPolling;
    private Button mPowerOn;
    private Button mPowerOff;
    private Button mTransmitApdu;
    private Button mTransmitEscape;
    private Button mGetDeviceInfo;
    private Button mGetBatteryLevel;
    private Button mGetBatteryStatus;
    private Button mGetCardStatus;
    private Button mSetTxPower;
    private Button mScan;
    private Button mPick;
    private Button btn_save;
    private Button btn_connect;
    private FloatingActionButton fab;

    private TextView mTxtConnectionState;
    private TextView mTxtAuthentication;
    private TextView mTxtATR;
    private TextView mTxtSlotStatus;
    private TextView mTxtResponseApdu;
    private TextView mTxtEscapeResponse;
    private TextView mTxtCardStatus;
    private TextView mTxtBatteryLevel;
    private TextView mTxtBatteryLevel2;
    private TextView mTxtBatteryStatus;
    private TextView mTxtBatteryStatus2;
    private TextView mTxtScan;
    private TextView rvTxtScan;

    private TextView mTxtSystemId;
    private TextView mTxtModelNo;
    private TextView mTxtSerialNo;
    private TextView mTxtFirmwareRev;
    private TextView mTxtHardwareRev;
    private TextView mTxtManufacturerName;
    private TextView txt_wtr;

    private EditText mEditMasterKey;
    private static EditText mEditApdu;
    private EditText mEditEscape;

    /* Detected reader. */
    private BluetoothReader mBluetoothReader;
    public static BluetoothReader mBluetoothReader2;
    /* ACS Bluetooth reader library. */
    private BluetoothReaderManager mBluetoothReaderManager;
    private BluetoothReaderGattCallback mGattCallback;

    private ProgressDialog mProgressDialog;

    /* Bluetooth GATT client. */
    private BluetoothGatt mBluetoothGatt;
    /*Recycler*/
    private RecyclerView recyclerViewParent;
    private RecyclerView recyclerViewDetail;
    private RecyclerView recyclerView2;
    private LinearLayoutManager layoutManager;
    private LinearLayoutManager layoutManager2;

    private Adapter.Callback callback_2;

    public static String uid_final;
    public static String no_WTR;
    public static String epoch;
    public static String id_wtr;
    public static String  wh_desc_id;
    public static String cat_selected;

    //private RecyclerViewAdapter adapter;
   // private RecyclerViewAdapterDetail adapter2;
    /*
    /*Expandable List*/


     /* Listen to Bluetooth bond status change event. And turns on reader's
     * notifications once the card reader is bonded.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothAdapter bluetoothAdapter = null;
            BluetoothManager bluetoothManager = null;
            final String action = intent.getAction();

            if (!(mBluetoothReader instanceof Acr3901us1Reader)) {
                /* Only ACR3901U-S1 require bonding. */
                return;
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "ACTION_BOND_STATE_CHANGED");

                /* Get bond (pairing) state */
                if (mBluetoothReaderManager == null) {
                    Log.w(TAG, "Unable to initialize BluetoothReaderManager.");
                    return;
                }

                bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager == null) {
                    Log.w(TAG, "Unable to initialize BluetoothManager.");
                    return;
                }

                bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null) {
                    Log.w(TAG, "Unable to initialize BluetoothAdapter.");
                    return;
                }

                final BluetoothDevice device = bluetoothAdapter
                        .getRemoteDevice(mDeviceAddress);

                if (device == null) {
                    return;
                }

                final int bondState = device.getBondState();


                Log.i(TAG, "BroadcastReceiver - getBondState. state = "
                        + getBondingStatusString(bondState));

                /* Enable notification */
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    if (mBluetoothReader != null) {
                        mBluetoothReader.enableNotification(true);
                        //new var for recycler view child
                        mBluetoothReader2.enableNotification(true);
                    }
                }

                /* Progress Dialog */
                if (bondState == BluetoothDevice.BOND_BONDING) {
                    mProgressDialog = ProgressDialog.show(context,
                            "ACR3901U-S1", "Bonding...");
                } else {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                }

                /*
                 * Update bond status and show in the connection status field.
                 */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTxtConnectionState
                                .setText(getBondingStatusString(bondState));
                    }
                });
            }
        }

    };


    /* Clear the Card reader's response and notification fields. */
    private void clearAllUi() {
        /* Clear notification fields. */
        mTxtAuthentication.setText(R.string.noData);

        /* Clear card reader's response fields. */
        clearResponseUi();
    }

    /* Clear the Card reader's Response field. */
    private void clearResponseUi() {
        mTxtAuthentication.setText(R.string.noData);
        mTxtATR.setText(R.string.noData);
        mTxtResponseApdu.setText(R.string.noData);
        mTxtEscapeResponse.setText(R.string.noData);
        mTxtScan.setText("");
    }

    private void findUiViews() {
        mAuthentication = (Button) findViewById(R.id.button_Authenticate);
        mStartPolling = (Button) findViewById(R.id.button_StartPolling);
        mStopPolling = (Button) findViewById(R.id.button_StopPolling);
        mTransmitApdu = (Button) findViewById(R.id.button_TransmitADPU);
        mScan = findViewById(R.id.btnScanSS);
        mPick = findViewById(R.id.btnPick);
        mTxtScan = findViewById(R.id.uid_2);
        mTransmitEscape = (Button) findViewById(R.id.button_TransmitEscapeCommand);
        mClear = (Button) findViewById(R.id.button_Clear);
        mTxtConnectionState = (TextView) findViewById(R.id.textView_ReaderState);
        mTxtAuthentication = (TextView) findViewById(R.id.textView_Authentication);
        mTxtATR = (TextView) findViewById(R.id.textView_ATR);
        mTxtResponseApdu = (TextView) findViewById(R.id.textView_Response);
        mTxtEscapeResponse = (TextView) findViewById(R.id.textView_EscapeResponse);
        mEditMasterKey = (EditText) findViewById(R.id.editText_Master_Key);
        mEditApdu = (EditText) findViewById(R.id.editText_ADPU);
        mEditEscape = (EditText) findViewById(R.id.editText_Escape);
        rvTxtScan = findViewById(R.id.uid);
        //btn_save= findViewById(R.id.save_to_wht);
        btn_connect = findViewById(R.id.button_Connect);
        fab=findViewById(R.id.fab_save);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReaderActivity.this, ItemWhtList.class);
                intent.putExtra("no_wtr", no_WTR);
                intent.putExtra("id_wtr",id_wtr);
                intent.putExtra("epoch",epoch);
                intent.putExtra("wh_desc_id",wh_desc_id);
                intent.putExtra("cat_selected",cat_selected);
                if(uid_final.equals("tes") ){
                    intent.putExtra("no_uid", "Belum Ada Device Ter-Scan");
                }else{
                    intent.putExtra("no_uid", uid_final);
                }
                startActivity(intent);
                //Log.d("intent", txt_title.getText().toString());

            }
        });
        btn_connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                connectReader();
            }
        });
    }

    /*
     * Update listener
     */
    private void setListener(BluetoothReader reader) {
        /* Update status change listener */
        if (mBluetoothReader instanceof Acr3901us1Reader) {
            ((Acr3901us1Reader) mBluetoothReader)
                    .setOnBatteryStatusChangeListener(new OnBatteryStatusChangeListener() {

                        @Override
                        public void onBatteryStatusChange(
                                BluetoothReader bluetoothReader,
                                final int batteryStatus) {

                            Log.i(TAG, "mBatteryStatusListener data: "
                                    + batteryStatus);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtBatteryStatus
                                            .setText(getBatteryStatusString(batteryStatus));
                                }
                            });
                        }

                    });
        } else if (mBluetoothReader instanceof Acr1255uj1Reader) {
            ((Acr1255uj1Reader) mBluetoothReader)
                    .setOnBatteryLevelChangeListener(new OnBatteryLevelChangeListener() {

                        @Override
                        public void onBatteryLevelChange(
                                BluetoothReader bluetoothReader,
                                final int batteryLevel) {

                            Log.i(TAG, "mBatteryLevelListener data: "
                                    + batteryLevel);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    mTxtBatteryLevel
//                                            .setText(getBatteryLevelString(batteryLevel));
                                }
                            });
                        }

                    });
        }
        mBluetoothReader
                .setOnCardStatusChangeListener(new OnCardStatusChangeListener() {

                    @Override
                    public void onCardStatusChange(
                            BluetoothReader bluetoothReader, final int sta) {

                        Log.i(TAG, "mCardStatusListener sta: " + sta);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                mTxtCardStatus
//                                        .setText(getCardStatusString(sta));
                            }
                        });
                    }

                });

        /* Wait for authentication completed. */
        mBluetoothReader
                .setOnAuthenticationCompleteListener(new OnAuthenticationCompleteListener() {

                    @Override
                    public void onAuthenticationComplete(
                            BluetoothReader bluetoothReader, final int errorCode) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                                    mTxtAuthentication
                                            .setText("Authentication Success!");
                                    mEditApdu.setText("FF CA 00 00 00");
                                    mAuthentication.setEnabled(false);

                                } else {
                                    mTxtAuthentication
                                            .setText("Authentication Failed!");
                                    btn_connect.setEnabled(true);
                                }
                            }
                        });
                    }

                });

        /* Wait for receiving ATR string. */
        mBluetoothReader
                .setOnAtrAvailableListener(new OnAtrAvailableListener() {

                    @Override
                    public void onAtrAvailable(BluetoothReader bluetoothReader,
                            final byte[] atr, final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (atr == null) {
                                    mTxtATR.setText(getErrorString(errorCode));
                                } else {
                                    mTxtATR.setText(Utils.toHexString(atr));
                                }
                            }
                        });
                    }

                });

        /* Wait for power off response. */
        mBluetoothReader
                .setOnCardPowerOffCompleteListener(new OnCardPowerOffCompleteListener() {

                    @Override
                    public void onCardPowerOffComplete(
                            BluetoothReader bluetoothReader, final int result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtATR.setText(getErrorString(result));
                            }
                        });
                    }

                });

        /* Wait for response APDU. */
        mBluetoothReader
                .setOnResponseApduAvailableListener(new OnResponseApduAvailableListener() {

                    @Override
                    public void onResponseApduAvailable(
                            BluetoothReader bluetoothReader, final byte[] apdu,
                            final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtResponseApdu.setText(getResponseString(
                                        apdu, errorCode));
                               // InnerTes("tes");
                            }
                        });
                    }

                });
        mBluetoothReader
                .setOnResponseApduAvailableListener(new OnResponseApduAvailableListener() {

                    @Override
                    public void onResponseApduAvailable(
                            BluetoothReader bluetoothReader, final byte[] apdu,
                            final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtScan.setText(getResponseString(
                                        apdu, errorCode));
                                 String uid = mTxtScan.getText().toString();
                                 uid_final= uid;
                                final String uid2=mTxtScan.getText().toString();
                                /*** Kirim Variabel String uid ke ChildviewHolder **/
                                /** code start here **/
                                Tes tes = new Tes() {


                                    @Override
                                    public String onClickTes(String uid) {
                                        return null;
                                    }

                                    @Override
                                    public String getUid() {
                                        return mTxtScan.getText().toString();

                                    }
                                };
                               // tes.onClickTes(uid);
                                uid_final = tes.getUid();
                                Log.d("uidFinal_listener"," "+uid_final);
                              //  requestUid(tes);


                                //uid_final="tes";
                               // InnerTes("tes");
//                                rvTxtScan.setText(getResponseString(
//                                        apdu, errorCode));
                                getInfoCard(uid);
                            }


                        });



                    }

                });


        /* Wait for escape command response. */
        mBluetoothReader
                .setOnEscapeResponseAvailableListener(new OnEscapeResponseAvailableListener() {

                    @Override
                    public void onEscapeResponseAvailable(
                            BluetoothReader bluetoothReader,
                            final byte[] response, final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtEscapeResponse.setText(getResponseString(
                                        response, errorCode));
                            }
                        });
                    }

                });

        /* Wait for device info available. */
        mBluetoothReader
                .setOnDeviceInfoAvailableListener(new OnDeviceInfoAvailableListener() {

                    @Override
                    public void onDeviceInfoAvailable(
                            BluetoothReader bluetoothReader, final int infoId,
                            final Object o, final int status) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status != BluetoothGatt.GATT_SUCCESS) {
                                    Toast.makeText(ReaderActivity.this,
                                            "Failed to read device info!",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                switch (infoId) {
                                case BluetoothReader.DEVICE_INFO_SYSTEM_ID: {
                                   // mTxtSystemId.setText(Utils.toHexString((byte[]) o));
                                }
                                    break;
                                case BluetoothReader.DEVICE_INFO_MODEL_NUMBER_STRING:
                                   // mTxtModelNo.setText((String) o);
                                    break;
                                case BluetoothReader.DEVICE_INFO_SERIAL_NUMBER_STRING:
                                   // mTxtSerialNo.setText((String) o);
                                    break;
                                case BluetoothReader.DEVICE_INFO_FIRMWARE_REVISION_STRING:
                                   // mTxtFirmwareRev.setText((String) o);
                                    break;
                                case BluetoothReader.DEVICE_INFO_HARDWARE_REVISION_STRING:
                                  //  mTxtHardwareRev.setText((String) o);
                                    break;
                                case BluetoothReader.DEVICE_INFO_MANUFACTURER_NAME_STRING:
                                    //mTxtManufacturerName.setText((String) o);
                                    break;
                                default:
                                    break;
                                }
                            }
                        });
                    }

                });

        /* Wait for battery level available. */
        if (mBluetoothReader instanceof Acr1255uj1Reader) {
            ((Acr1255uj1Reader) mBluetoothReader)
                    .setOnBatteryLevelAvailableListener(new OnBatteryLevelAvailableListener() {

                        @Override
                        public void onBatteryLevelAvailable(
                                BluetoothReader bluetoothReader,
                                final int batteryLevel, int status) {
                            Log.i(TAG, "mBatteryLevelListener data: " + batteryLevel);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                  //  mTxtBatteryLevel2.setText(getBatteryLevelString(batteryLevel));
                                }
                            });

                        }

                    });
        }

        /* Handle on battery status available. */
        if (mBluetoothReader instanceof Acr3901us1Reader) {
            ((Acr3901us1Reader) mBluetoothReader)
                    .setOnBatteryStatusAvailableListener(new OnBatteryStatusAvailableListener() {

                        @Override
                        public void onBatteryStatusAvailable(
                                BluetoothReader bluetoothReader,
                                final int batteryStatus, int status) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                   // mTxtBatteryStatus2.setText(getBatteryStatusString(batteryStatus));
                                }
                            });
                        }

                    });
        }

        /* Handle on slot status available. */
        mBluetoothReader
                .setOnCardStatusAvailableListener(new OnCardStatusAvailableListener() {

                    @Override
                    public void onCardStatusAvailable(
                            BluetoothReader bluetoothReader,
                            final int cardStatus, final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (errorCode != BluetoothReader.ERROR_SUCCESS) {
                                    mTxtSlotStatus
                                            .setText(getErrorString(errorCode));
                                } else {
                                    mTxtSlotStatus
                                            .setText(getCardStatusString(cardStatus));
                                }
                            }
                        });
                    }

                });

        mBluetoothReader
                .setOnEnableNotificationCompleteListener(new OnEnableNotificationCompleteListener() {

                    @Override
                    public void onEnableNotificationComplete(
                            BluetoothReader bluetoothReader, final int result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result != BluetoothGatt.GATT_SUCCESS) {
                                    /* Fail */
                                    Toast.makeText(
                                            ReaderActivity.this,
                                            "The device is unable to set notification!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ReaderActivity.this,
                                            "The device is ready to use!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                });
    }

    private void setListener2(BluetoothReader reader) {
        /* Update status change listener */

        /* Wait for authentication completed. */
        mBluetoothReader2
                .setOnAuthenticationCompleteListener(new OnAuthenticationCompleteListener() {

                    @Override
                    public void onAuthenticationComplete(
                            BluetoothReader bluetoothReader, final int errorCode) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                                    mTxtAuthentication
                                            .setText("Authentication Success!");
                                    mEditApdu.setText("FF CA 00 00 00");
                                    mAuthentication.setEnabled(false);
                                } else {
                                    mTxtAuthentication
                                            .setText("Authentication Failed!");
                                }
                            }
                        });
                    }

                });

        /* Wait for receiving ATR string. */
        mBluetoothReader2
                .setOnAtrAvailableListener(new OnAtrAvailableListener() {

                    @Override
                    public void onAtrAvailable(BluetoothReader bluetoothReader,
                                               final byte[] atr, final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (atr == null) {
                                    mTxtATR.setText(getErrorString(errorCode));
                                } else {
                                    mTxtATR.setText(Utils.toHexString(atr));
                                }
                            }
                        });
                    }

                });

        /* Wait for power off response. */
        mBluetoothReader2
                .setOnCardPowerOffCompleteListener(new OnCardPowerOffCompleteListener() {

                    @Override
                    public void onCardPowerOffComplete(
                            BluetoothReader bluetoothReader, final int result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtATR.setText(getErrorString(result));
                            }
                        });
                    }

                });

        /* Wait for response APDU. */
        mBluetoothReader2
                .setOnResponseApduAvailableListener(new OnResponseApduAvailableListener() {

                    @Override
                    public void onResponseApduAvailable(
                            BluetoothReader bluetoothReader, final byte[] apdu,
                            final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtResponseApdu.setText(getResponseString(
                                        apdu, errorCode));
                                // InnerTes("tes");
                            }
                        });
                    }

                });
        mBluetoothReader2
                .setOnResponseApduAvailableListener(new OnResponseApduAvailableListener() {

                    @Override
                    public void onResponseApduAvailable(
                            BluetoothReader bluetoothReader, final byte[] apdu,
                            final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtScan.setText(getResponseString(
                                        apdu, errorCode));
                                String uid = mTxtScan.getText().toString();
                                uid_final= uid;
                                final String uid2=mTxtScan.getText().toString();
                                /*** Kirim Variabel String uid ke ChildviewHolder **/
                                /** code start here **/
                                Tes tes = new Tes() {


                                    @Override
                                    public String onClickTes(String uid) {
                                        return null;
                                    }

                                    @Override
                                    public String getUid() {
                                        return mTxtScan.getText().toString();

                                    }
                                };
                                tes.onClickTes(uid);
                                uid_final = tes.getUid();
                                Log.d("uidFinal_listener"," "+uid_final);
                                requestUid(tes);


                                //uid_final="tes";
                                // InnerTes("tes");
//                                rvTxtScan.setText(getResponseString(
//                                        apdu, errorCode));
                                getInfoCard(uid);
                            }


                        });



                    }

                });


        /* Wait for escape command response. */
        mBluetoothReader2
                .setOnEscapeResponseAvailableListener(new OnEscapeResponseAvailableListener() {

                    @Override
                    public void onEscapeResponseAvailable(
                            BluetoothReader bluetoothReader,
                            final byte[] response, final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTxtEscapeResponse.setText(getResponseString(
                                        response, errorCode));
                            }
                        });
                    }

                });

        /* Wait for device info available. */
        mBluetoothReader2
                .setOnDeviceInfoAvailableListener(new OnDeviceInfoAvailableListener() {

                    @Override
                    public void onDeviceInfoAvailable(
                            BluetoothReader bluetoothReader, final int infoId,
                            final Object o, final int status) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status != BluetoothGatt.GATT_SUCCESS) {
                                    Toast.makeText(ReaderActivity.this,
                                            "Failed to read device info!",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                switch (infoId) {
                                    case BluetoothReader.DEVICE_INFO_SYSTEM_ID: {
                                        // mTxtSystemId.setText(Utils.toHexString((byte[]) o));
                                    }
                                    break;
                                    case BluetoothReader.DEVICE_INFO_MODEL_NUMBER_STRING:
                                        // mTxtModelNo.setText((String) o);
                                        break;
                                    case BluetoothReader.DEVICE_INFO_SERIAL_NUMBER_STRING:
                                        // mTxtSerialNo.setText((String) o);
                                        break;
                                    case BluetoothReader.DEVICE_INFO_FIRMWARE_REVISION_STRING:
                                        // mTxtFirmwareRev.setText((String) o);
                                        break;
                                    case BluetoothReader.DEVICE_INFO_HARDWARE_REVISION_STRING:
                                        //  mTxtHardwareRev.setText((String) o);
                                        break;
                                    case BluetoothReader.DEVICE_INFO_MANUFACTURER_NAME_STRING:
                                        //mTxtManufacturerName.setText((String) o);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                    }

                });

        /* Wait for battery level available. */
        if (mBluetoothReader2 instanceof Acr1255uj1Reader) {
            ((Acr1255uj1Reader) mBluetoothReader2)
                    .setOnBatteryLevelAvailableListener(new OnBatteryLevelAvailableListener() {

                        @Override
                        public void onBatteryLevelAvailable(
                                BluetoothReader bluetoothReader,
                                final int batteryLevel, int status) {
                            Log.i(TAG, "mBatteryLevelListener data: " + batteryLevel);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //  mTxtBatteryLevel2.setText(getBatteryLevelString(batteryLevel));
                                }
                            });

                        }

                    });
        }

        /* Handle on battery status available. */
        if (mBluetoothReader2 instanceof Acr3901us1Reader) {
            ((Acr3901us1Reader) mBluetoothReader2)
                    .setOnBatteryStatusAvailableListener(new OnBatteryStatusAvailableListener() {

                        @Override
                        public void onBatteryStatusAvailable(
                                BluetoothReader bluetoothReader,
                                final int batteryStatus, int status) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // mTxtBatteryStatus2.setText(getBatteryStatusString(batteryStatus));
                                }
                            });
                        }

                    });
        }

        /* Handle on slot status available. */
        mBluetoothReader2
                .setOnCardStatusAvailableListener(new OnCardStatusAvailableListener() {

                    @Override
                    public void onCardStatusAvailable(
                            BluetoothReader bluetoothReader,
                            final int cardStatus, final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (errorCode != BluetoothReader.ERROR_SUCCESS) {
                                    mTxtSlotStatus
                                            .setText(getErrorString(errorCode));
                                } else {
                                    mTxtSlotStatus
                                            .setText(getCardStatusString(cardStatus));
                                }
                            }
                        });
                    }

                });

        mBluetoothReader2
                .setOnEnableNotificationCompleteListener(new OnEnableNotificationCompleteListener() {

                    @Override
                    public void onEnableNotificationComplete(
                            BluetoothReader bluetoothReader, final int result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result != BluetoothGatt.GATT_SUCCESS) {
                                    /* Fail */
                                    Toast.makeText(
                                            ReaderActivity.this,
                                            "The device is unable to set notification!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ReaderActivity.this,
                                            "The device is ready to use!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                });
    }


    /* Set Button onClick() events. */
    private void setOnClickListener() {
        /*
         * Update onClick listener.
         */

        /* Clear UI text. */
        mClear.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                clearResponseUi();
            }
        });

        /* Authentication function, authenticate the connected card reader. */
        mAuthentication.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mBluetoothReader == null) {
                    mTxtAuthentication.setText(R.string.card_reader_not_ready);
                    return;
                }
                Log.d("Blue_auth",mBluetoothReader.toString());
                Log.d("Blue_auth",mEditMasterKey.toString());

                /* Retrieve master key from edit box. */
                byte masterKey[] = Utils.getEditTextinHexBytes(mEditMasterKey);
                Log.d("Blue_auth",masterKey.toString());

                if (masterKey != null && masterKey.length > 0) {
                    /* Clear response field for the result of authentication. */
                    mTxtAuthentication.setText(R.string.noData);

                    /* Start authentication. */
                    if (!mBluetoothReader.authenticate(masterKey)) {
                        mTxtAuthentication
                                .setText(R.string.card_reader_not_ready);
                    } else {
                        mTxtAuthentication.setText("Authenticating...");
                    }
                } else {
                    mTxtAuthentication.setText("Character format error!");
                }
            }
        });

        /* Start polling card. */
        mStartPolling.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mBluetoothReader == null) {
                    mTxtATR.setText(R.string.card_reader_not_ready);
                    return;
                }
                if (!mBluetoothReader.transmitEscapeCommand(AUTO_POLLING_START)) {
                    mTxtATR.setText(R.string.card_reader_not_ready);
                }
            }
        });

        /* Stop polling card. */
        mStopPolling.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mBluetoothReader == null) {
                    mTxtATR.setText(R.string.card_reader_not_ready);
                    return;
                }
                if (!mBluetoothReader.transmitEscapeCommand(AUTO_POLLING_STOP)) {
                    mTxtATR.setText(R.string.card_reader_not_ready);
                }
            }
        });

        /* Transmit ADPU. */
        mTransmitApdu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                /* Check for detected reader. */
        if (mBluetoothReader == null) {
            mTxtResponseApdu.setText(R.string.card_reader_not_ready);
            return;
        }

        /* Retrieve APDU command from edit box. */

        byte apduCommand[] = Utils.getEditTextinHexBytes(mEditApdu);

        if (apduCommand != null && apduCommand.length > 0) {
            /* Clear response field for result of APDU. */
            mTxtResponseApdu.setText(R.string.noData);

            /* Transmit APDU command. */
            if (!mBluetoothReader.transmitApdu(apduCommand)) {
                mTxtResponseApdu
                        .setText(R.string.card_reader_not_ready);
            }
        } else {
            mTxtResponseApdu.setText("Character format error!");
        }
    }
});

        /* Transmit escape command. */
        mTransmitEscape.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                /* Check for detected reader. */
                if (mBluetoothReader == null) {
                    mTxtEscapeResponse.setText(R.string.card_reader_not_ready);
                    return;
                }

                /* Retrieve escape command from edit box. */
                byte escapeCommand[] = Utils.getEditTextinHexBytes(mEditEscape);

                if (escapeCommand != null && escapeCommand.length > 0) {
                    /* Clear response field for result of escape command. */
                    mTxtEscapeResponse.setText(R.string.noData);

                    /* Transmit escape command. */
                    if (!mBluetoothReader.transmitEscapeCommand(escapeCommand)) {
                        mTxtEscapeResponse
                                .setText(R.string.card_reader_not_ready);
                    }
                } else {
                    mTxtEscapeResponse.setText("Character format error!");
                }
            }
        });

    }


    /* Start the process to enable the reader's notifications. */
    private void activateReader(BluetoothReader reader) {
        if (reader == null) {
            return;
        }

        if (reader instanceof Acr3901us1Reader) {
            /* Start pairing to the reader. */
            ((Acr3901us1Reader) mBluetoothReader).startBonding();
        } else if (mBluetoothReader instanceof Acr1255uj1Reader) {
            /* Enable notification. */
            mBluetoothReader.enableNotification(true);
        }
    }

    ArrayList<ChildItem> dataParent = new ArrayList<>();
    Adapter<ChildItem, WtrChildViewHolder> adapter = new Adapter<ChildItem, WtrChildViewHolder>
            (R.layout.list_item_detail_2, WtrChildViewHolder.class, dataParent){

        @Override
        protected void bindView(WtrChildViewHolder holder, final ChildItem model, final int position) {
            holder.onBind(ReaderActivity.this, model);
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(ReaderActivity.this, "Click on " + model.getKode_barang()+" position :"+position, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    Adapter.Callback adapterListeners = new Adapter.Callback() {

        @Override
        public void onImageClick(int position) {
           setOnClickListener();
        }

        @Override
        public void onRemoveItem(int position) {
            setOnClickListener();
        }
    };
   // ArrayList<ChildItem> dataChild = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);
            uid_final="tes";
            Log.d("uidFinal"," "+uid_final);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        no_WTR=intent.getStringExtra("no_WTR");
        epoch = intent.getStringExtra("epoch");
        id_wtr=intent.getStringExtra("id_wtr");
        wh_desc_id=intent.getStringExtra("wh_desc_id");
        cat_selected = intent.getStringExtra("cat_selected");

        Log.d("URL_re", "onCreate: "+no_WTR);
        txt_wtr=findViewById(R.id.wtr_display);
        txt_wtr.setText(no_WTR);

       // final View target = findViewById(R.id.circle_collapsed_target);
        //final CircleImageView circleImageView = (CircleImageView) findViewById(R.id.circle_image_view);


        //Harus satu jaringan waktu develope krn hosting lokal
        requestJsonObjectA(new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
//                dataParent.add(new ParentModel("Judul ke-"));
//                Log.d("data_parent_i",dataParent.toString());
                for(int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject jsonObject = result.getJSONObject(i);
                        String no_WTR2= no_WTR;
                        String kode_barang = jsonObject.getString("kode_barang");
                        String nama_barang = jsonObject.getString("nama_barang");
                        String qty = jsonObject.getString("qty");
                        String picked_qty = jsonObject.getString("picked_qty");
                        String sisa_qty = jsonObject.getString("sisa_qty");
                        //Log.d("no_WTR", judul);
                        dataParent.add(new ChildItem(no_WTR2,kode_barang,nama_barang,qty,picked_qty,sisa_qty));
                       // dataChild.add(new ChildItem(judul));
                    }
                    catch(JSONException e) {
                        dataParent.add(new ChildItem("Error: " + e.getLocalizedMessage(),"err","err","err","err","err"));
                    }
                }
                recyclerViewParent = (RecyclerView) findViewById(R.id.recycler_view);
                setUpView();
            }
        },no_WTR);
        recyclerViewParent = (RecyclerView) findViewById(R.id.recycler_view);
        setUpView();



        /* Update UI. */
        findUiViews();
        updateUi(null);
        //buttonSave

        /* Set the onClick() event handlers. */
        setOnClickListener();

        /* Initialize BluetoothReaderGattCallback. */
        mGattCallback = new BluetoothReaderGattCallback();

        /* Register BluetoothReaderGattCallback's listeners */
        mGattCallback
                .setOnConnectionStateChangeListener(new OnConnectionStateChangeListener() {

                    @Override
                    public void onConnectionStateChange(
                            final BluetoothGatt gatt, final int state,
                            final int newState) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state != BluetoothGatt.GATT_SUCCESS) {
                                    /*
                                     * Show the message on fail to
                                     * connect/disconnect.
                                     */
                                    mConnectState = BluetoothReader.STATE_DISCONNECTED;

                                    if (newState == BluetoothReader.STATE_CONNECTED) {
                                        mTxtConnectionState
                                                .setText(R.string.connect_fail);
                                    } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                                        mTxtConnectionState
                                                .setText(R.string.disconnect_fail);
                                    }
                                    clearAllUi();
                                    updateUi(null);
                                    invalidateOptionsMenu();
                                    return;
                                }

                                updateConnectionState(newState);

                                if (newState == BluetoothProfile.STATE_CONNECTED) {
                                    /* Detect the connected reader. */
                                    if (mBluetoothReaderManager != null) {
                                        mBluetoothReaderManager.detectReader(
                                                gatt, mGattCallback);
                                    }
                                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                    mBluetoothReader = null;
                                    mBluetoothReader2=null;
                                    /*
                                     * Release resources occupied by Bluetooth
                                     * GATT client.
                                     */
                                    if (mBluetoothGatt != null) {
                                        mBluetoothGatt.close();
                                        mBluetoothGatt = null;
                                    }
                                }
                            }
                        });
                    }
                });

        /* Initialize mBluetoothReaderManager. */
        mBluetoothReaderManager = new BluetoothReaderManager();

        /* Register BluetoothReaderManager's listeners */
        mBluetoothReaderManager
                .setOnReaderDetectionListener(new OnReaderDetectionListener() {

                    @Override
                    public void onReaderDetection(BluetoothReader reader) {
                        updateUi(reader);

                        if (reader instanceof Acr3901us1Reader) {
                            /* The connected reader is ACR3901U-S1 reader. */
                            Log.v(TAG, "On Acr3901us1Reader Detected.");
                        } else if (reader instanceof Acr1255uj1Reader) {
                            /* The connected reader is ACR1255U-J1 reader. */
                            Log.v(TAG, "On Acr1255uj1Reader Detected.");
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ReaderActivity.this,
                                            "The device is not supported!",
                                            Toast.LENGTH_SHORT).show();

                                    /* Disconnect Bluetooth reader */
                                    Log.v(TAG, "Disconnect reader!!!");
                                    disconnectReader();
                                    updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
                                }
                            });
                            return;
                        }

                        mBluetoothReader = reader;
                        mBluetoothReader2= reader;
                        setListener(reader);
                        setListener2(reader);

                        activateReader(reader);
                    }
                });

        /* Connect the reader. */
        connectReader();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        getSupportActionBar().setTitle(mDeviceName);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpView() {
        recyclerViewParent.setLayoutManager(new LinearLayoutManager(ReaderActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerViewParent.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        adapter.setCallback(adapterListeners);

    }

    private void getInfoCard(String uid) {
       // requestJsonCard(uid);
        Toast.makeText(ReaderActivity.this,"UID kartu : "+uid,Toast.LENGTH_SHORT).show();

    }

    protected void requestJsonObjectA(final VolleyCallback callback,String no_WTR) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.1.250.116/rest-api/index.php/api/Item_WTR/"+no_WTR;
       // String url ="http://192.168.0.4/rest-api/index.php/api/Item_WTR/"+no_WTR;
        Log.d("URL_reader", "requestJsonObjectA: "+url);
        //String url ="http://192.168.0.5/rest-api/index.php/api/Item_2/";
        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray jsonArray) {
                callback.onSuccess(jsonArray);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(request);
    }
    protected String requestUid(Tes t){
       String s= t.getUid();
        return  s;
    }
    public String requestUid_2 (){
        Log.d("uidFinal_method"," "+uid_final);
        /* Check for detected reader. */
        if (mBluetoothReader2 == null) {
            uid_final="card not ready yet";
            return uid_final;
        }

        /* Retrieve APDU command from edit box. */
        mEditApdu.setText("FF CA 00 00 00");
        byte apduCommand[] = Utils.getEditTextinHexBytes(mEditApdu);

        if (apduCommand != null && apduCommand.length > 0) {
            /* Clear response field for result of APDU. */
           //mTxtResponseApdu.setText(R.string.noData);
            //uid_final="no data";

            /* Transmit APDU command. */
            if (!mBluetoothReader2.transmitApdu(apduCommand)) {
                uid_final="card not ready yet";
                return uid_final;
            }
        } else {
            uid_final="card not ready yet";
            return uid_final;
        }
        return uid_final;
    }

    public interface Tes {
        String onClickTes(String uid);
        String getUid();

    }

    public interface VolleyCallback{
        void onSuccess(JSONArray result);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();

        final IntentFilter intentFilter = new IntentFilter();

        /* Start to monitor bond state change */
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, intentFilter);

        /* Clear unused dialog. */
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();

        /* Stop to monitor bond state change */
        unregisterReceiver(mBroadcastReceiver);

        /* Disconnect Bluetooth reader */
        disconnectReader();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        /* Save the current state. */
//        savedInstanceState.putString(BATTERY_LEVEL_STRING, mTxtBatteryLevel
//                .getText().toString());
//        savedInstanceState.putString(BATTERY_LEVEL_2_STRING, mTxtBatteryLevel2
//                .getText().toString());
//        savedInstanceState.putString(BATTERY_STATUS_STRING, mTxtBatteryStatus
//                .getText().toString());
//        savedInstanceState.putString(BATTERY_STATUS_2_STRING,
//                mTxtBatteryStatus2.getText().toString());
//        savedInstanceState.putString(SLOT_STATUS_STRING, mTxtSlotStatus
//                .getText().toString());
//        savedInstanceState.putString(SLOT_STATUS_2_STRING, mTxtCardStatus
//                .getText().toString());

//        savedInstanceState.putString(AUTHENTICATION_KEY, mEditMasterKey
//                .getText().toString());
//        savedInstanceState.putString(ATR_STRING, mTxtATR.getText().toString());
//        savedInstanceState.putString(APDU_COMMAND, mEditApdu.getText()
//                .toString());
//        savedInstanceState.putString(RESPONSE_APDU, mTxtResponseApdu.getText()
//                .toString());
//        savedInstanceState.putString(RESPONSE_APDU, mTxtScan.getText()
//                .toString());
//        savedInstanceState.putString(ESCAPE_COMMAND, mEditEscape.getText()
//                .toString());
//        savedInstanceState.putString(ESCAPE_RESPONSE, mTxtEscapeResponse
//                .getText().toString());
//
//        savedInstanceState.putString(DEVICE_INFO_SYSTEM_ID, mTxtSystemId
//                .getText().toString());
//        savedInstanceState.putString(DEVICE_INFO_MODEL_NUM_STRING, mTxtModelNo
//                .getText().toString());
//        savedInstanceState.putString(DEVICE_INFO_SERIAL_NUM_STRING,
//                mTxtSerialNo.getText().toString());
//        savedInstanceState.putString(DEVICE_INFO_FW_REV_STRING, mTxtFirmwareRev
//                .getText().toString());
//        savedInstanceState.putString(DEVICE_INFO_HW_REV_STRING, mTxtHardwareRev
//                .getText().toString());
//        savedInstanceState.putString(DEVICE_INFO_MANUFACTURER_NAME_STRING,
//                mTxtManufacturerName.getText().toString());

        /* Always call the superclass so it can save the view hierarchy state. */
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        /* Always call the superclass so it can restore the view hierarchy. */
        super.onRestoreInstanceState(savedInstanceState);

        /* Restore state members from saved instance. */
//        mTxtBatteryLevel.setText(savedInstanceState
//                .getString(BATTERY_LEVEL_STRING));
//        mTxtBatteryLevel2.setText(savedInstanceState
//                .getString(BATTERY_LEVEL_2_STRING));
//        mTxtBatteryStatus.setText(savedInstanceState
//                .getString(BATTERY_STATUS_STRING));
//        mTxtBatteryStatus2.setText(savedInstanceState
//                .getString(BATTERY_STATUS_2_STRING));
//        mTxtSlotStatus
//                .setText(savedInstanceState.getString(SLOT_STATUS_STRING));
//        mTxtCardStatus.setText(savedInstanceState
//                .getString(SLOT_STATUS_2_STRING));
//
//        mEditMasterKey
//                .setText(savedInstanceState.getString(AUTHENTICATION_KEY));
//        mTxtATR.setText(savedInstanceState.getString(ATR_STRING));
//        mEditApdu.setText(savedInstanceState.getString(APDU_COMMAND));
//        mTxtResponseApdu.setText(savedInstanceState.getString(RESPONSE_APDU));
//        mTxtScan.setText(savedInstanceState.getString(RESPONSE_APDU));
//        mEditEscape.setText(savedInstanceState.getString(ESCAPE_COMMAND));
//        mTxtEscapeResponse.setText(savedInstanceState
//                .getString(ESCAPE_RESPONSE));
//
//        mTxtSystemId.setText(savedInstanceState
//                .getString(DEVICE_INFO_SYSTEM_ID));
//        mTxtModelNo.setText(savedInstanceState
//                .getString(DEVICE_INFO_MODEL_NUM_STRING));
//        mTxtSerialNo.setText(savedInstanceState
//                .getString(DEVICE_INFO_SERIAL_NUM_STRING));
//        mTxtFirmwareRev.setText(savedInstanceState
//                .getString(DEVICE_INFO_FW_REV_STRING));
//        mTxtHardwareRev.setText(savedInstanceState
//                .getString(DEVICE_INFO_HW_REV_STRING));
//        mTxtManufacturerName.setText(savedInstanceState
//                .getString(DEVICE_INFO_MANUFACTURER_NAME_STRING));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
//        getMenuInflater().inflate(R.menu.reader, menu);
//        if (mConnectState == BluetoothReader.STATE_CONNECTED) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_connecting).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else if (mConnectState == BluetoothReader.STATE_CONNECTING) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_connecting).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//            menu.findItem(R.id.menu_refresh).setActionView(
//                    R.layout.actionbar_indeterminate_progress);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_connecting).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case R.id.menu_connect:
//            /* Connect Bluetooth reader */
//            Log.v(TAG, "Start to connect!!!");
//            connectReader();
//            return true;
//        case R.id.menu_connecting:
//        case R.id.menu_disconnect:
//            /* Disconnect Bluetooth reader */
//            Log.v(TAG, "Start to disconnect!!!");
//            disconnectReader();
//            return true;
//        case android.R.id.home:
//            onBackPressed();
//            return true;
//        case R.id.menu_about:
//            DialogFragment fragment = new VersionInfoDialogFragment();
//            fragment.show(getSupportFragmentManager(), "VersionInfo");
//            return true;
//        default:
//            break;
//        }
       return super.onOptionsItemSelected(item);
    }

    /* Show and hide UI resources and set the default master key and commands. */
    private void updateUi(final BluetoothReader bluetoothReader) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bluetoothReader instanceof Acr3901us1Reader) {
                    /* The connected reader is ACR3901U-S1 reader. */
                    if (mEditMasterKey.getText().length() == 0) {
                        mEditMasterKey.setText(DEFAULT_3901_MASTER_KEY);
                    }
                    if (mEditApdu.getText().length() == 0) {
                        mEditApdu.setText(DEFAULT_3901_APDU_COMMAND);
                    }
                    if (mEditEscape.getText().length() == 0) {
                        mEditEscape.setText(DEFAULT_3901_ESCAPE_COMMAND);
                    }
                    mClear.setEnabled(true);
                    mAuthentication.setEnabled(true);
                    mStartPolling.setEnabled(false);
                    mStopPolling.setEnabled(false);
                    btn_connect.setEnabled(true);
//                    mPowerOn.setEnabled(true);
//                    mPowerOff.setEnabled(true);
                    mTransmitApdu.setEnabled(true);
                    mTransmitEscape.setEnabled(true);
//                    mGetDeviceInfo.setEnabled(true);
//                    mGetBatteryLevel.setEnabled(false);
//                    mGetBatteryStatus.setEnabled(true);
//                    mGetCardStatus.setEnabled(true);
//                    mSetTxPower.setEnabled(false);
                    mEditMasterKey.setEnabled(true);
                    mEditApdu.setEnabled(true);
                    mEditEscape.setEnabled(true);
                } else if (bluetoothReader instanceof Acr1255uj1Reader) {
                    /* The connected reader is ACR1255U-J1 reader. */
                    if (mEditMasterKey.getText().length() == 0) {
                        try {
                            mEditMasterKey.setText(Utils
                                    .toHexString(DEFAULT_1255_MASTER_KEY
                                            .getBytes("UTF-8")));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mEditApdu.getText().length() == 0) {
                        mEditApdu.setText(DEFAULT_1255_APDU_COMMAND);
                    }
                    if (mEditEscape.getText().length() == 0) {
                        mEditEscape.setText(DEFAULT_1255_ESCAPE_COMMAND);
                    }
                    mClear.setEnabled(true);
                    mAuthentication.setEnabled(true);
                    mStartPolling.setEnabled(true);
                    mStopPolling.setEnabled(true);
                    btn_connect.setEnabled(false);
//                    mPowerOn.setEnabled(true);
//                    mPowerOff.setEnabled(true);
                   mTransmitApdu.setEnabled(true);
                   mTransmitEscape.setEnabled(true);
//                    mGetDeviceInfo.setEnabled(true);
//                    mGetBatteryLevel.setEnabled(true);
//                    mGetBatteryStatus.setEnabled(false);
//                    mGetCardStatus.setEnabled(true);
//                    mSetTxPower.setEnabled(true);
                    mEditMasterKey.setEnabled(true);
                    mEditApdu.setEnabled(true);
                    mEditEscape.setEnabled(true);
                } else {
                    mEditApdu.setText(R.string.noData);
                    mEditEscape.setText(R.string.noData);
                    mClear.setEnabled(true);
                    mAuthentication.setEnabled(false);
                    mStartPolling.setEnabled(false);
                    mStopPolling.setEnabled(false);
                    btn_connect.setEnabled(true);
//                    mPowerOn.setEnabled(false);
//                    mPowerOff.setEnabled(false);
                    mTransmitApdu.setEnabled(false);
                    mTransmitEscape.setEnabled(false);
//                    mGetDeviceInfo.setEnabled(false);
//                    mGetBatteryLevel.setEnabled(false);
//                    mGetBatteryStatus.setEnabled(false);
//                    mGetCardStatus.setEnabled(false);
//                    mSetTxPower.setEnabled(false);
                    mEditMasterKey.setEnabled(false);
                    mEditApdu.setEnabled(false);
                    mEditEscape.setEnabled(false);
                }
            }
        });
    }

    /*
     * Create a GATT connection with the reader. And detect the connected reader
     * once service list is available.
     */
    private boolean connectReader() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.w(TAG, "Unable to initialize BluetoothManager.");
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return false;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.w(TAG, "Unable to obtain a BluetoothAdapter.");
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return false;
        }

        /*
         * Connect Device.
         */
        /* Clear old GATT connection. */
        if (mBluetoothGatt != null) {
            Log.i(TAG, "Clear old GATT connection");
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        /* Create a new connection. */
        final BluetoothDevice device = bluetoothAdapter
                .getRemoteDevice(mDeviceAddress);

        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }

        /* Connect to GATT server. */
        updateConnectionState(BluetoothReader.STATE_CONNECTING);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }

    /* Disconnects an established connection. */
    private void disconnectReader() {
        if (mBluetoothGatt == null) {
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return;
        }
        updateConnectionState(BluetoothReader.STATE_DISCONNECTING);
        mBluetoothGatt.disconnect();
    }

    /* Get the Battery level string. */
    private String getBatteryLevelString(int batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100) {
            return "Unknown.";
        }
        return String.valueOf(batteryLevel) + "%";
    }

    /* Get the Battery status string. */
    private String getBatteryStatusString(int batteryStatus) {
        if (batteryStatus == BluetoothReader.BATTERY_STATUS_NONE) {
            return "No battery.";
        } else if (batteryStatus == BluetoothReader.BATTERY_STATUS_FULL) {
            return "The battery is full.";
        } else if (batteryStatus == BluetoothReader.BATTERY_STATUS_USB_PLUGGED) {
            return "The USB is plugged.";
        }
        return "The battery is low.";
    }

    /* Get the Bonding status string. */
    private String getBondingStatusString(int bondingStatus) {
        if (bondingStatus == BluetoothDevice.BOND_BONDED) {
            return "BOND BONDED";
        } else if (bondingStatus == BluetoothDevice.BOND_NONE) {
            return "BOND NONE";
        } else if (bondingStatus == BluetoothDevice.BOND_BONDING) {
            return "BOND BONDING";
        }
        return "BOND UNKNOWN.";
    }

    /* Get the Card status string. */
    private String getCardStatusString(int cardStatus) {
        if (cardStatus == BluetoothReader.CARD_STATUS_ABSENT) {
            return "Absent.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_PRESENT) {
            return "Present.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWERED) {
            return "Powered.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWER_SAVING_MODE) {
            return "Power saving mode.";
        }
        return "The card status is unknown.";
    }

    /* Get the Error string. */
    private String getErrorString(int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            return "";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_CHECKSUM) {
            return "The checksum is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA_LENGTH) {
            return "The data length is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_COMMAND) {
            return "The command is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_UNKNOWN_COMMAND_ID) {
            return "The command ID is unknown.";
        } else if (errorCode == BluetoothReader.ERROR_CARD_OPERATION) {
            return "The card operation failed.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
            return "Authentication is required.";
        } else if (errorCode == BluetoothReader.ERROR_LOW_BATTERY) {
            return "The battery is low.";
        } else if (errorCode == BluetoothReader.ERROR_CHARACTERISTIC_NOT_FOUND) {
            return "Error characteristic is not found.";
        } else if (errorCode == BluetoothReader.ERROR_WRITE_DATA) {
            return "Write command to reader is failed.";
        } else if (errorCode == BluetoothReader.ERROR_TIMEOUT) {
            return "Timeout.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_FAILED) {
            return "Authentication is failed.";
        } else if (errorCode == BluetoothReader.ERROR_UNDEFINED) {
            return "Undefined error.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA) {
            return "Received data error.";
        } else if (errorCode == BluetoothReader.ERROR_COMMAND_FAILED) {
            return "The command failed.";
        }
        return "Unknown error.";
    }

    /* Get the Response string. */
    private String getResponseString(byte[] response, int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            if (response != null && response.length > 0) {
                return Utils.toHexString(response);
            }
            return "";
        }
        return getErrorString(errorCode);
    }



    /* Update the display of Connection status string. */
    private void updateConnectionState(final int connectState) {

        mConnectState = connectState;

        if (connectState == BluetoothReader.STATE_CONNECTING) {
            mTxtConnectionState.setText(R.string.connecting);
            btn_connect.setEnabled(true);
        } else if (connectState == BluetoothReader.STATE_CONNECTED) {
            mTxtConnectionState.setText(R.string.connected);
            btn_connect.setEnabled(true);
        } else if (connectState == BluetoothReader.STATE_DISCONNECTING) {
            mTxtConnectionState.setText(R.string.disconnecting);
            btn_connect.setEnabled(true);
        } else {
            mTxtConnectionState.setText(R.string.disconnected);
            clearAllUi();
            updateUi(null);
            btn_connect.setEnabled(true);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onDialogItemClick(DialogFragment dialog, int which) {

        byte[] command = { (byte) 0xE0, 0x00, 0x00, 0x49, (byte) which };

        if (mBluetoothReader == null) {

            mTxtATR.setText(R.string.card_reader_not_ready);
            return;
        }

        if (!mBluetoothReader.transmitEscapeCommand(command)) {
            mTxtATR.setText(R.string.card_reader_not_ready);
        }
    }
}
