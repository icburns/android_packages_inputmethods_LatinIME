/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin;

import android.content.Context;

import com.android.inputmethod.compat.InputMethodInfoCompatWrapper;
import com.android.inputmethod.compat.InputMethodManagerCompatWrapper;
import com.android.inputmethod.compat.InputMethodSubtypeCompatWrapper;

import java.util.Collections;
import java.util.List;

public class SubtypeUtils {
    private SubtypeUtils() {
        // This utility class is not publicly instantiable.
    }

    // TODO: Cache my InputMethodInfo and/or InputMethodSubtype list.
    public static boolean checkIfSubtypeBelongsToThisIme(Context context,
            InputMethodSubtypeCompatWrapper ims) {
        final InputMethodManagerCompatWrapper imm = InputMethodManagerCompatWrapper.getInstance();
        if (imm == null) return false;

        final InputMethodInfoCompatWrapper myImi = getInputMethodInfo(context.getPackageName());
        final List<InputMethodSubtypeCompatWrapper> subtypes =
                imm.getEnabledInputMethodSubtypeList(myImi, true);
        for (final InputMethodSubtypeCompatWrapper subtype : subtypes) {
            if (subtype.equals(ims)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasMultipleEnabledIMEsOrSubtypes(
            final boolean shouldIncludeAuxiliarySubtypes) {
        final InputMethodManagerCompatWrapper imm = InputMethodManagerCompatWrapper.getInstance();
        if (imm == null) return false;

        final List<InputMethodInfoCompatWrapper> enabledImis = imm.getEnabledInputMethodList();
        return hasMultipleEnabledSubtypes(shouldIncludeAuxiliarySubtypes, enabledImis);
    }

    public static boolean hasMultipleEnabledSubtypesInThisIme(Context context,
            final boolean shouldIncludeAuxiliarySubtypes) {
        final InputMethodInfoCompatWrapper myImi = getInputMethodInfo(context.getPackageName());
        final List<InputMethodInfoCompatWrapper> imiList = Collections.singletonList(myImi);
        return hasMultipleEnabledSubtypes(shouldIncludeAuxiliarySubtypes, imiList);
    }

    private static boolean hasMultipleEnabledSubtypes(final boolean shouldIncludeAuxiliarySubtypes,
            List<InputMethodInfoCompatWrapper> imiList) {
        final InputMethodManagerCompatWrapper imm = InputMethodManagerCompatWrapper.getInstance();
        if (imm == null) return false;

        // Number of the filtered IMEs
        int filteredImisCount = 0;

        for (InputMethodInfoCompatWrapper imi : imiList) {
            // We can return true immediately after we find two or more filtered IMEs.
            if (filteredImisCount > 1) return true;
            final List<InputMethodSubtypeCompatWrapper> subtypes =
                    imm.getEnabledInputMethodSubtypeList(imi, true);
            // IMEs that have no subtypes should be counted.
            if (subtypes.isEmpty()) {
                ++filteredImisCount;
                continue;
            }

            int auxCount = 0;
            for (InputMethodSubtypeCompatWrapper subtype : subtypes) {
                if (subtype.isAuxiliary()) {
                    ++auxCount;
                }
            }
            final int nonAuxCount = subtypes.size() - auxCount;

            // IMEs that have one or more non-auxiliary subtypes should be counted.
            // If shouldIncludeAuxiliarySubtypes is true, IMEs that have two or more auxiliary
            // subtypes should be counted as well.
            if (nonAuxCount > 0 || (shouldIncludeAuxiliarySubtypes && auxCount > 1)) {
                ++filteredImisCount;
                continue;
            }
        }

        if (filteredImisCount > 1) {
            return true;
        }
        final List<InputMethodSubtypeCompatWrapper> subtypes =
                imm.getEnabledInputMethodSubtypeList(null, true);
        int keyboardCount = 0;
        // imm.getEnabledInputMethodSubtypeList(null, true) will return the current IME's
        // both explicitly and implicitly enabled input method subtype.
        // (The current IME should be LatinIME.)
        for (InputMethodSubtypeCompatWrapper subtype : subtypes) {
            if (SubtypeSwitcher.KEYBOARD_MODE.equals(subtype.getMode())) {
                ++keyboardCount;
            }
        }
        return keyboardCount > 1;
    }

    public static String getInputMethodId(String packageName) {
        return getInputMethodInfo(packageName).getId();
    }

    public static InputMethodInfoCompatWrapper getInputMethodInfo(String packageName) {
        final InputMethodManagerCompatWrapper imm = InputMethodManagerCompatWrapper.getInstance();
        if (imm == null) {
            throw new RuntimeException("Input method manager not found");
        }

        for (final InputMethodInfoCompatWrapper imi : imm.getEnabledInputMethodList()) {
            if (imi.getPackageName().equals(packageName))
                return imi;
        }
        throw new RuntimeException("Can not find input method id for " + packageName);
    }
}
