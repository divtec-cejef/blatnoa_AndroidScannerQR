package com.divtec.blatnoa.scannerqr;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MessageFragment extends Fragment {

    final int SEND_DELAY = 3500;

    private EditText phoneNum;
    private EditText message;
    private Button btnSend;

    private Bundle latLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        latLng = getActivity().getIntent().getExtras();

        // Set a listener to get the new coordinates
        getParentFragmentManager().setFragmentResultListener("latLng", this,
            (requestKey, result) -> {
                if (message != null
                    && message.getText().toString().startsWith(getString(R.string.default_msg))) {
                    // Get the bundle from the fragment manager
                    latLng = result;
                    updateDefaultMessage();
                }
        });

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Get the components from the view
        phoneNum = view.findViewById(R.id.edit_phone);
        message = view.findViewById(R.id.edit_message);
        btnSend = view.findViewById(R.id.btn_send);

        // TODO Remove this
        phoneNum.setText("0766817585");

        // Set the default message
        updateDefaultMessage();

        // Set the onClickListener for the button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneNum.getText().toString().trim().replaceAll(" ", "");
                if (checkPhoneNum(phone)) {
                    sendSMS(phone, message.getText().toString());
                } else {
                    invalidPhoneNumDialog();
                }
            }
        });
    }

    /**
     * Update the default message with the coordinates
     */
    private void updateDefaultMessage() {
        double roundedLatitude = roundTo(latLng.getDouble("latitude"), 4);
        double roundedLongitude = roundTo(latLng.getDouble("longitude"), 4);

        message.setText(getString(R.string.default_msg)
                + roundedLatitude + ", "
                + roundedLongitude);
    }

    /**
     * Checks if the phone number is valid
     * @param phoneNum the phone number to check
     * @return whether the phone number is valid or not
     */
    private boolean checkPhoneNum(String phoneNum) {
        // Check if the phone number is valid
        return PhoneNumberUtils.isGlobalPhoneNumber(phoneNum);
    }

    /**
     * Show a dialog informing the user that the phone number is invalid
     */
    private void invalidPhoneNumDialog() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Invalid phone number")
                .setMessage("Please enter a valid phone number")
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        phoneNum.requestFocus();
                    }
                }).
                setPositiveButton("OK", null)
                .show();
    }

    /**
     * Send a message by SMS to a phone number
     * @param phoneNum the phone number to send the message to
     * @param message the message to send
     */
    private void sendSMS(String phoneNum, String message) {
        // Check if the app has the permission to send SMS
        checkForSmsPermission();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNum, null, message, null, null);

            Toast.makeText(getContext(), "Message sent", Toast.LENGTH_SHORT).show();

            // Temporarily disable the button to avoid spamming
            deactivateButtonFor(SEND_DELAY);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Message couldn't be sent !", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if the app has permission to send SMS messages.
     * If not, requests the permission.
     */
    private void checkForSmsPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) { // If sms permission is not granted
            // Request sms permissions
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }

    /**
     * Round a double to a certain number of decimals
     * @param number the number to round
     * @param decimals the number of decimals to round to
     * @return the rounded number
     */
    private double roundTo(double number,int decimals) {
        double multiplier = Math.pow(10, decimals);
        return Math.round(number * multiplier) / multiplier;
    }

    /**
     * Deactivates the send button for a certain amount of time
     * @param millis the amount of time to deactivate the button
     */
    private void deactivateButtonFor(int millis) {
        if (millis > 0) {
            btnSend.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnSend.setEnabled(true);
                }
            }, millis);
        }
    }
}