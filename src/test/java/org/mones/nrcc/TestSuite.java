package org.mones.nrcc;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectPackages("org.mones.nrcc")
@IncludeClassNamePatterns({"^.*Test$"})
public class TestSuite { }
