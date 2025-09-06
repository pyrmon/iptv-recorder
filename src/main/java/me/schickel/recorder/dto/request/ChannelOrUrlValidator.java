package me.schickel.recorder.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChannelOrUrlValidator implements ConstraintValidator<ChannelOrUrlRequired, RecordingScheduleRequest> {

    @Override
    public boolean isValid(RecordingScheduleRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }
        
        boolean hasChannel = request.getChannel() != null && !request.getChannel().trim().isEmpty();
        boolean hasUrl = request.getM3uUrl() != null && !request.getM3uUrl().trim().isEmpty();
        
        return hasChannel || hasUrl;
    }
}