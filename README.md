GlassAuth
=========

A library for making OAuth with Google easier!

## About ##

GlassAuth is an easy to use library designed to make authentication with a Google Account easier. Have you seen applications using QR codes to send OAuth tokens to Glass? That's not quite a good idea. GlassAuth makes use of Google's OAuth for Devices. All the user has to do is enter a code [here](http://www.google.com/device).

GlassAuth takes care of making a slick sign in process (with UI and all), and makes authorized HTTPS requests in just a few lines.

## Setup ##
It is expected that you have a project setup at the [Google Developer Console](https://console.developers.google.com/), with the Google+ API enabled and OAuth certificates generated. 

## How to Use ##
1. Clone the source of this application and include it as a library in your Glass project. 
2. In the GlassAuth library, go to `src/com/victor/kaiser/pendergrast/auth/setup/AuthConstants.java` and set the `CLIENT_ID` and `CLIENT_SECRET` according to what you generated at the Google Developer Console.
3. Use manifest merger, or declare the `AuthActivity` in your manifest like so: `<activity android:name="com.victork.kaiser.pendergrast.auth.AuthActivity" />`
4. In your UI, have the user sign in by starting an `AuthActivity`
5. Later, you can confirm the user is signed in by calling `AuthHelper.isAuthenticated()`
6. Use `AuthHttpsRequest` to  make authenticated requests. You can add JSON parameters and change the HTTP request method to whatever you need. The `OnResponseListener` you specify will be called when a response is received.
                AuthHttpsTask request = new AuthHttpsTask(context, url, json, "POST", listener);
                request.execute();

## Screenshots ##

![Tap to Continue Screen](/screenshots/screen0.png)

![Enter Code Screen](/screenshots/screen1.png)

![Finished Screen](/screenshots/screen2.png)

## Notes ##
The `onResponse(boolean success, String response)` method in `OnResponseListener` may not behave as you expect. `success` only indicates if the HTTPS request returned 200 "good" code. `Response` is simply the response from the server.

For some more background on this method, see my [GlassNotes](https://github.com/victorkp/GlassWebNotes) repo, or this [presentation](https://docs.google.com/presentation/d/1aAJKwQVzJbMrJSfc9p3B_puEb9k1GvsrBcZIUaRDjAY/edit?usp=sharing).
