FlashNotifier
=============

Android app project for CS 371m (MWF 1-2pm )

Group Members: Henri Zac Sweers (HS7737), Nick Pape (NAP626), Edward Lee(EL8366)

How to use the app: 
Our application gives the user the option to determine whether or not they want to be notified for phone calls and 
sms messages via the camera flash. In addition, we plan on implementin an open API, meaning that othe applications
wll be allowed to use our app as part of their notifications. For example, it would be possible for Whatsapp to alert
the user of an incoming message via our open API, assuming that Whatsapp includes the necessary code and the user has
allowed api privleges. The inteface is pretty simple. Starting from the main page there is a toggle (on/off) that 
determines whether or not the service that we have created for the app is running. In addition, there are checkboxes 
that determine whether or not sms and 

Features of the app that have been implemented that were not part of the application prototype:
Implemented backwards compatiblity with previous Android builds in terms of the action bar through the use o . 

List of classes and main chunks of code obtained from other sources:
Most of the code in this application is original except for the code used from ActionBarSherlock, which allows for
backwards comaptibliy wih previous . 
The only reference we have is Torch.java which we used in reference in order
to understand how to toggle the comera' s led flash on/off.

List of classes and major chunks of code we implemented ourselves: 
Classes include: Main.java, APIAccess.java, and SMSCallListener.java. Torch.java by Colin McDonough is the exception 
as we used it for reference when developing our code when
