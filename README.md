# webapp

## Student Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
| Dhaval Suthar | 001475572 | suthar.d@husky.neu.edu |

## Technology Stack

The Bill Tracking application is developed using Java Spring Boot framework that uses the REST architecture to create,
update and retrieve User and Bills and Spring Security to secure GET & PUT & Delete api calls.

## Build Instructions

Pre-req : Need POSTMAN and MySQL installed.
 1. Clone repository git@github.com:suthard-spring2020/webapp.git
 2. Import Maven Dependencies
 3. Run application by traversing to webapp/Webapplication
 4. Through IDE, under src tab, find Webapplication.java and run that file.
 5. Through command line: mvn clean install and then mvn test

## Deploy Instructions
    * Open POSTMAN
    * To Create User -
        - Use v1/user & No Authentication 
        - Proivde below json format in raw and format type json:
            {
              "first_name": "Dhaval",
              "last_name": "Suthar",
              "password": "Dhaval$1234",
              "emailId": "dhaval@gmail.com"
             }
        - If all the fiels are correct will return Success : 200 OK
            {
                "uuid": "b4afeb77-8c20-4843-aa57-fd9c2da494ee",
                "first_name": "Dhaval",
                "last_name": "Suthar",
                "emailId": "dhaval@gmail.com",
                "creationTime": "2020-01-26T23:48:37.928+0000",
                "updatedTime": "2020-01-26T23:48:37.928+0000"
            }
            
         - If Validation fails FAILURE : 400 BAD REQUEST
    
    * To Get User -
        - Use v1/user/self & set Authentication to Basic Auth
        - Success : 200 OK
            {
                "uuid": "b4afeb77-8c20-4843-aa57-fd9c2da494ee",
                "first_name": "Dhaval",
                "last_name": "Suthar",
                "emailId": "dhaval@gmail.com",
                "creationTime": "2020-01-26T23:48:37.928+0000",
                "updatedTime": "2020-01-26T23:48:37.928+0000"
            }
        - Failure : 401 UNAUTHORIZED     
            Access Denied
            
    * To Update User
        - Use v1/user/self & set Authentication to Basic Auth
        - Success : 204 NO CONTENT
        - Failure : 400 BAD REQUEST
        
    * To Create a Bill 
        - Use v1/bill/ & set Authentication to Basic Auth
        - Success : 200 
            {
                "id": "75da0038-1ca2-4c8a-b381-6e8776f928dc",
                "owner_id": "b4afeb77-8c20-4843-aa57-fd9c2da494ee",
                "vendor": "Northeastern University",
                "bill_date": "2020-01-06T00:00:00.000+0000",
                "due_date": "2020-01-12T00:00:00.000+0000",
                "amount_due": 7000.51,
                "creationTime": "2020-01-26T23:48:42.797+0000",
                "updatedTime": "2020-01-26T23:48:42.797+0000",
                "categories": [
                     "college",
                     "spring2020",
                    "tuition"
                ],
                "payment_status": "paid"
                }
        - Not Found : 404 //wrong url
        - UnAuthorized : 401 //unauthorized
             Access Denied
                
    * To retrieve all the bills of specefic user
        - Use v1/bills/ & set Authentication to Basic Auth
        - Success : 200 
            {
                "id": "c969471b-d836-4036-b925-c809a81ecbc9",
                "owner_id": "b4afeb77-8c20-4843-aa57-fd9c2da494ee",
                "vendor": "Northeastern University",
                "bill_date": "2020-01-06T00:00:00.000+0000",
                "due_date": "2020-01-12T00:00:00.000+0000",
                "amount_due": 200.0,
                "creationTime": "2020-01-26T23:48:46.197+0000",
                "updatedTime": "2020-01-26T23:48:46.197+0000",
                "categories": [
                    "college"
                ],
                "payment_status": "paid"
            }
        - No content : 204 //if no bills        
        - Not Found : 404 //wrong url
        - UnAuthorized : 401 //unauthorized
            Access Denied
            
    * To retrieve bill based on id/ to delete bill based on id/ to update bill based on id
        - Use v1/bill/{id] & set Authentication to Basic Auth
        * For retrieve bill i.e. Get request
        -Success : 200
            {
                "id": "e61dfed9-a078-4bb5-aa5e-cc9d1ebe473d",
                "owner_id": "554ded24-27d9-4660-8ef5-912aaf0314ef",
                "vendor": "Northeastern University",
                "bill_date": "2020-01-06T00:00:00.000+0000",
                "due_date": "2020-01-12T00:00:00.000+0000",
                "amount_due": 7000.51,
                "creationTime": "2020-01-26T20:18:12.000+0000",
                "updatedTime": "2020-01-26T20:18:12.000+0000",
                "categories": [
                    "college",
                    "spring2020",
                    "tuition"
                ],
                "payment_status": "paid"
            }
        - No content : 204 //if no bills        
        - Not Found : 404 //wrong url
        - UnAuthorized : 401 //unauthorized
            Access Denied
                 
        * For delete bill i.e. Delete Request
        - Success : 200
        - No content : 204 //if no bills        
        - Not Found : 404 //wrong url
        - UnAuthorized : 401 //unauthorized
            Access Denied
        
        * For update bill i.e. PUT request
        - Success : 200
        - No content : 204 //if no bills        
        - Not Found : 404 //wrong url
        - UnAuthorized : 401 //unauthorized
            Access Denied
                    
## Running Tests

1. Implemented Junit using Mockito for unit testing for creation of user.
2. Run Unit Test by traversing into cd test and run UserServiceTest.java

## CircleCI
1. Pr_check job : which will compile and run unit test on each pull request raised on organization master branch
2. build_deploy job: which will compile and run unit test on merging pull request on organization master branch
      
        

