# Jmeter-reusable-code
This is repo is created to store the jmeter scripts with complex level logic.

Script 1:auth-script-globallock-JWT
Script 2:Chat selenium UI script
Script 3:Email Script - general script with common logical implementation 
Script 4:Chat script - Async call in JSR223 samplers. 
Script 5:WSS based websocket based chat script 

Scripts are uploaded as it is and may not run on user machine as it may have local file reference. Many scripts requires additional plugins to runs- when you open Jmeter may suggest to install them. 


## Auth-script-globallock-JWT Requirment 

What did you learn while building this project? What challenges did you face and how did you overcome them?

Auth-script-globallock-JWT Requirment -

Call Auth requests once every 15min/when Auth token is expred and should be genearted only once across the thread.
Generate the claims JWT token based on the private key and PKCS8EncodedKeySpec key
Solution: Based on the critical section cortoller , and Global_lock only once for all the the thread token will be generated, we are setting the create time stamp when it is generated and after every call we are calculating the time difference so that after 15min we can call the Auth token call again. Logic is written in Beanshell pre-post sampler, assertion.

## WebSocket_Chat_Script.jmx
Here WSS - web sokcet based frame wise communication is implemented in Jmeter
Lot of encoding, decoding and regex, oatter matcher function use to create the chat message. 

## Selenium_UI_perfTesting.jmx

Here, we have implemented Selenium based script in webdriver sampler. to calculate Ui loading time for chat page. 

## Mail

This script again has lot of generic usable code. JSR223 code to log the error, repeat the request 2 times before marking it failed etc. 

## Chat 
Here, chat is happening using http protocol with JSR223 groovy code.Complex communication based on polling mechanism to fetch the new response etc. 

## few useful code: 

This is generic useful code wrote in sinle sampler, based on requirement one can modify and use that. 
## Authors

- [@Kothawadeprasanna](https://github.com/kothawadeprasanna)

