package org.mones.nrcc;

/**
 * Utility class for data used in tests.
 */
public abstract class TestData {

	public static final String[] DATA_SET_1 =
			new String[] {"000000001", "000000002", "000000003", "000000004"};
	public static final String[] DATA_SET_2 =
			new String[] {"100000000", "200000000", "300000000", "400000000"};
	public static final String[] DATA_SET_T =
			new String[] {"000000000", "000000000", "000000000", "000000000", "terminate"};
	public static final String[] DATA_SET_K =
			new String[] {"terminate"};

	/**
	 * Write period for the DATA_SET_T
	 */
	public static final int T_WAIT = 5000;
}