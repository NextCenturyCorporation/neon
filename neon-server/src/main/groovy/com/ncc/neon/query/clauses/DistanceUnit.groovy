/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.query.clauses

import com.ncc.neon.query.jackson.DistanceUnitDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize



/**
 * A representation of a distance
 */
@JsonDeserialize(using = DistanceUnitDeserializer)
public enum DistanceUnit {


    METER(1), KM(1000), MILE(1609.34)


    /** the distance unit in meters */
    def meters

    DistanceUnit(meters) {
        this.meters = meters
    }

}