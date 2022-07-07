# Jmeter-reusable-code
This is repo is created to store the jmeter scripts with complex level logic.

Script 1:auth-script-globallock-JWT
Script 2:Chat selenium UI script
Script 3:Email Script - general script with common logical implementation 
Script 4:Chat script - Async call in JSR223 samplers. 

Scripts are uploaded as it is and may not run on user machine as it may have local file reference. Many scripts requires additional plugins to runs- when you open Jmeter may suggest to install them. 


## Script1

What did you learn while building this project? What challenges did you face and how did you overcome them?

Auth-script-globallock-JWT Requirment -

Call Auth requests once every 15min/when Auth token is expred and should be genearted only once across the thread.
Generate the claims JWT token based on the private key and PKCS8EncodedKeySpec key
Solution: Based on the critical section cortoller , and Global_lock only once for all the the thread token will be generated, we are setting the create time stamp when it is generated and after every call we are calculating the time difference so that after 15min we can call the Auth token call again. Logic is written in Beanshell pre-post sampler, assertion.

## Authors

- [@Kothawadeprasanna](https://github.com/kothawadeprasanna)

