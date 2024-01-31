@configuration
Feature: KarmaPlatform has configuration

  The KarmaPlatform has configuration and is loading it from a file

  Scenario: Configuration is available
    When KarmaPlatform is created with "test-config.properties"
    Then KarmaPlatform has a configuration object instantiated
    And The configuration file is loaded

