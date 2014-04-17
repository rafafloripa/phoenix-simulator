Feature: 
  As a developer
  I want to subscribe for a specific signal
  So that I can use it in my application

  @shutdownNode
  Scenario: subscribing for a signal
    Given The simulator is setup and running
    And The simulator is providing 150 with value 1
    And DummyApp subscribes for signal 150
    And After 8000 mSec have passed
    When The simulator changes the signal 150 to 10
    And After 100 mSec have passed
    Then DummyApp should get a notification for signal 150 with value 10
