Feature: 
As a developer
I want to subscribe for a specific signal
So that I can use it in my application

  Scenario: subscribing for a signal
    Given The simulator is setup and running
    And The simulator is providing 0x0001 with value 1
    And DummyApp subscribes for signal 0x0001
    And After 3000 mSec have passed
    When The simulator changes the signal 0x0001 to 10
    Then DummyApp should get a notification for signal 0x0001 with value 10
