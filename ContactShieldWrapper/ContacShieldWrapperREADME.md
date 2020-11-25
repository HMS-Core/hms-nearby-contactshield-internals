**ContactShieldWrapper Guidance：**

- Integration of HMS ContactShield

  Modify the gradle file：

  ```
  // Project gradle
  buildscript {
      repositories {
          maven { url 'http://developer.huawei.com/repo/' }
      }
  }
  
  allprojects {
      repositories {
          maven {url 'http://developer.huawei.com/repo/'}
      }
  }
  
  // app gradle
  implementation 'com.huawei.hms:contactshield:5.0.3.300'
  ```

- Apply ContactShieldWrapper

  ```
  // Get ContactShieldWrapper instance
  ContactShieldWrapper contactShieldWrapper = ContactShieldWrapper.getInstance(context);
  
  // Call HMS ContactShield API with ContactShieldWrapper instance
  contactShieldWrapper.start();
  contactShieldWrapper.stop();
  contactShieldWrapper.isEnable();
  contactShieldWrapper.getTemporaryExposureKeyHistory();
  contactShieldWrapper.provideDiagnosisKeys(files, exposureConfiguration, token);
  contactShieldWrapper.getExposureSummary(token);
  contactShieldWrapper.getExposureInformation(token);
  
  // Convert data types with static methods of ContactShieldWrapper
  ContactShieldWrapper.getExposureSummary(contactSketch);
  ContactShieldWrapper.getExposureInformationList(contactDetailList);
  ContactShieldWrapper.getTemporaryExposureKeyList(periodicKeyList);
  
  // Add BackgroundContactShieldIntentService in AndroidManifest.xml
  <service android:name=".nearby.BackgroundContactShieldIntentService"
            android:enabled="true"
            android:exported="true"/>
  ```

  