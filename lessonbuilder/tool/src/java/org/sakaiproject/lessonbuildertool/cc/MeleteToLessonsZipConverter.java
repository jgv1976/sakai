package org.sakaiproject.lessonbuildertool.cc;

/***********
 * This code is based on a reference implementation done for the IMS Consortium.
 * The copyright notice for that implementation is included below.
 * All modifications are covered by the following copyright notice.
 *
 * Copyright (c) 2016 APEREO
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2010 IMS GLobal Learning Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

import org.springframework.web.multipart.MultipartFile;

public class MeleteToLessonsZipConverter {

    private MultipartFile cc;
    private String siteId;
    private Boolean displayOption; //One only page or one page per module


    public MeleteToLessonsZipConverter(MultipartFile the_cc, String the_siteId, Boolean cpOnePage) {
        this.cc = the_cc;
        this.siteId = the_siteId;
        this.displayOption = cpOnePage;
    }

    public MultipartFile convert() {
        return cc;
    }
    //HERE THE CODE THAT TAKES THE MELETE CP ZIP FILE AND MAKES IT A IMS CC ZIP FILE.

}
