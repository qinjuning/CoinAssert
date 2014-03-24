package com.coinwtb.coinassistant.utils;

public class Slog {
	private static boolean LOG_DEBUG = true;

	protected static void setDebug(boolean debug) {
		LOG_DEBUG = debug;
	}
	
	public static void v(String tag, String msg) {
		if (LOG_DEBUG) {
			android.util.Log.v(tag, msg + " ## " + getTracePrefix("d"));
		}
	}

	public static void d(String tag, String msg) {
		if (LOG_DEBUG) {
			android.util.Log.d(tag, msg + " ## " + getTracePrefix("d"));
		}
	}

	public static void i(String tag, String msg) {
		if (LOG_DEBUG) {
			android.util.Log.i(tag, msg + " ## " + getTracePrefix("i"));
		}
	}

	public static void w(String tag, String msg) {
		if (LOG_DEBUG) {
			android.util.Log.w(tag, msg + " ## " + getTracePrefix("w"));
		}
	}

	public static void e(String tag, String msg) {
		if (LOG_DEBUG) {
			android.util.Log.e(tag, msg + " ## " + getTracePrefix("e"));
		}
	}
	
    private static String getTracePrefix( String logLevel ) {
        StackTraceElement[] sts = new Throwable().getStackTrace();
        StackTraceElement st = null;
        for ( int i = 0; i < sts.length; i++ ) {
            if ( sts[i].getMethodName().equalsIgnoreCase( logLevel ) && i + 2 < sts.length ) {

                if ( sts[i + 1].getMethodName().equalsIgnoreCase( logLevel ) ) {
                    st = sts[i + 2];
                    break;
                }
                else {
                    st = sts[i + 1];
                    break;
                }
            }
        }
        if ( st == null ) {
            return "";
        }

        String clsName = st.getClassName();
        if ( clsName.contains( "$" ) ) {
            clsName = clsName.substring( clsName.lastIndexOf( "." ) + 1, clsName.indexOf( "$" ) );
        }
        else {
            clsName = clsName.substring( clsName.lastIndexOf( "." ) + 1 );
        }
        return clsName + "-> " + st.getMethodName() + "():";
    }
	
	
}
