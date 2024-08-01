# Linker: Simple Link Sharing with NFC!

## What Is This App About?
This app aims to take advantage of Android's Host-based card emulation capabilities on NFC devices in order to easily share links or text with other NFC capable devices **(regardless if they are Android or not)** just as if the sharing device was a NFC tag. Importantly, this app aims to be convenient and easy to use as it is available in the native Android share menu! Additionally, this app is separated into Android Mobile and Wear OS components. This allows the user to also use their Wear OS watch to share links or text provided from the mobile component.

## How Do You Use It?
When sharing links or text, simply select the app from the native Android share menu. Then, simply physically tap another receiving NFC device against the sharing device. Additionally, if the Wear OS component is installed, you may press the button on the bottom of the dialog to toggle between sharing from your phone or watch!

## Screenshots
<p align="center">
  <img src="https://github.com/user-attachments/assets/447e6aa6-65ec-44b2-8d13-be0ed0faca65" width=300 />
  <img src="https://github.com/user-attachments/assets/f23db91e-952b-482a-a29b-a5f4925933eb" width=300 />
</p>

---

<p align="center">
  <img src="https://github.com/user-attachments/assets/81779cfd-ff04-439f-9adb-9a68941b21ac" width=300 />
  <img src="https://github.com/user-attachments/assets/494fb1fd-11e6-4675-ab35-7bc8ffb5dd6e" width=300 />
</p>

## Installing
Apks for both the mobile and Wear OS components are provided in the Releases tab. Installing the mobile component simply requires downloading the mobile apk on your device through a web browser and then installing it. However, installing the Wear OS component may be difficult as it requires that the component be installed through ADB debugging and tools.

## IMPORTANT, IF YOU ARE USING GALAXY WATCH

In your watch's settings in `Connections >> NFC and contactless payments >> Others`, make sure to disable `Samsung Health` as this conflicts with the app.
<p align="center">
  <img src="https://github.com/user-attachments/assets/c8fd3bc0-1948-42a5-9e47-01b0b474b5d2" width=300 />
</p>

## Credits
All credit for the NDEF tag emulation and HCE implementation in the ApduService class belongs to the author of https://github.com/MichaelsPlayground/NfcHceNdefEmulator and the author(s) of https://github.com/TechBooster/C85-Android-4.4-Sample. The respective Apache 2.0 license notice is included in this project's implementation: `ApduService.java`. Without the insight and code provided in these repositories, this app would not be possible.
