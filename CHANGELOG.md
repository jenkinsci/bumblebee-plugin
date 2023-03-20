Changelog

===
### Version 4.1.9 (released March, 2023) 

- Fix compatibility with the latest Jenkins versions running on Java 11+
- Upgrade httpclient version to 4.5.13

### Version 4.1.8 (released May, 2021) 

-	Move documentation into GitHub

### Version 4.1.7 (released May, 2021) 

-	Add support for UFT reports in native format

### Version 4.1.6 (released December, 2020)

-	Add support for Allure reports
-	Fix for SECURITY-2156 - Encrypt all passwords (even encrypted by Bumblebee) with
Jenkins Secret so they cannot be recovered from XML config file

### Version 4.1.5 (released August, 2020)

-	Add "HP ALM Auto Defect Management" feature

### Version 4.1.4 (released September, 2019)

-   Add "Trust to self-signed certificats" checkbox
-   Replace Jersey library with Apache HTTP Client

### Version 4.1.3 (released July, 2019)

-   Fix bug with failure on save of "Import HP ALM Test Results" build
    step

### Version 4.1.2 (released February, 2019)

-   Update dependencies

### Version 4.1.1 (released January, 2019)

-   Add support of Serenity reports
-   Add support of JBehave reports
-   Add ability to skip connectivity diagnostic
-   Add ability to override PC user/password

### Version 4.1.0 (released April, 2018)

-   Support protractor-jasmine reports
-   Add retry settings for collate/analyze phase
-   Improve logging
-   Bugfixes

### Version 4.0.9 (released February, 2018)

-   Pull results from HP ALM
-   Improve logging

### Version 4.0.8 (released October, 2017)

-   FIx possible memory leak

### Version 4.0.7 (released September, 2017)

-   Bug fixes

### Version 4.0.6 (released July, 2017)

-   Add new "Bumblebee: Add Test to Test Set" step for creating TestSet
    instances in HP ALM TestLab and adding tests from TestPlan to them

### Version 4.0.5 (released June, 2017)

-   Running local HP UFT tests from Jenkins
-   Running HP Performance Center tests from Jenkins

### Version 4.0.4 (released August, 2016)

-   Use Jenkins proxy settings to communicate with Bumblebee and HP ALM
    server
-   Bug fixes

### Version 4.0.3 (released July, 2016)

-   Bug fixes

### Version 4.0.2 (released July, 2016)

-   Support for Bumblebee server version 4.0.4
-   Asynchronous processing of test reports
    ([docs](http://www.agiletestware.com/docs/bumblebee-docs/en/latest/ci-integration/jenkins/#offline-asynchronous-processing-of-test-reports))
-   Running HP ALM tests from Jenkins
    ([docs](http://www.agiletestware.com/docs/bumblebee-docs/en/latest/ci-integration/jenkins/#running-tests-in-hp-alm-from-jenkins))
-   Add support of Cucumber reports
-   Add support of FitNesse reports

### Version 4.0.1 (released February, 2016)

-   Downgrade to Java 1.6 to support older versions of Jenkins

### Version 4.0.0 (released October, 2015)

-   Support for Bumblebee server version 4.0
-   Usability improvements

### Version 3.0.2 (released November, 2014)

-   Updated Jenkins dependency version
-   Fixed minor bug with license checker logic.

### Version 3.0.0 (released November, 2014)

-   Bumblebee releases version 3.0
