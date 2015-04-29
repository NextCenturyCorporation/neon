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

/**
 * This class has a list of the available channels that are used for messaging between widgets
 * @class neon.eventing.channels
 */

neon.eventing.channels = {
    /**
     * @property SELECTION_CHANGED
     * @type {string}
     */
    SELECTION_CHANGED: 'selection_changed',

    /**
     * @property FILTERS_CHANGED
     * @type {string}
     */
    FILTERS_CHANGED: 'filters_changed',

    /**
     * @property CONNECT_TO_HOST
     * @type {string}
     */
    CONNECT_TO_HOST: 'connect_to_host'
};
