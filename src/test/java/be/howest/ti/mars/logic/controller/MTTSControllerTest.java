package be.howest.ti.mars.logic.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MTTSControllerTest {

    @Test
    void getMessageReturnsAWelcomeMessage() {
        // Arrange
        MTTSController sut = new MTTSController();

        // Act
        String message = sut.getMessage();

        //Assert
        assertTrue(StringUtils.isNoneBlank(message));
    }
}
