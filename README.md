# healthTracker
This is an Android Library  to track human mobility using a phone's sensor.
It creates a never ending service tracking a number of sensors. Default ws step counter, activity recognition and location manager. In the settings you can select other sensors such as gyro, accelerometer etc.

The interface is very basic. Especially although it requests all the correct permissions, the text asking for permission is lacking. In particular the location permission request is definitely not acceptable for a real world application (it needs far more explanations) and the terms and condition screen is empty.
You will have to implement your own pages for that. 
