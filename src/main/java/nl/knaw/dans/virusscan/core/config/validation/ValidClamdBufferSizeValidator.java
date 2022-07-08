/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.virusscan.core.config.validation;

import nl.knaw.dans.virusscan.core.config.ClamdConfig;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidClamdBufferSizeValidator implements ConstraintValidator<ValidClamdBufferSize, ClamdConfig> {
    @Override
    public boolean isValid(ClamdConfig clamdConfig, ConstraintValidatorContext constraintValidatorContext) {

        // validate that each value is significantly bigger than the next one
        if (clamdConfig.getChunksize() / 2 < clamdConfig.getOverlapsize()) {
            return false;
        }

        if (clamdConfig.getOverlapsize() / 2 < clamdConfig.getBuffersize()) {
            return false;
        }

        return true;
    }
}
