package info.guardianproject.mrapp;

import info.guardianproject.mrapp.lessons.LessonManager;
import info.guardianproject.mrapp.server.ServerManager;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import net.sqlcipher.database.SQLiteDatabase;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

public class StoryMakerApp extends Application {

	
	private static ServerManager mServerManager;
	private static LessonManager mLessonManager;
	
	private final static String LOCALE_DEFAULT = "en";//need to force english for now as default
	private final static String LANG_ARABIC = "ar";
	
	private static Locale mLocale = new Locale(LOCALE_DEFAULT);
	
	private final static String PREF_LOCALE = "plocale";
	
	private static String mBaseUrl = null;
	
	private final static String URL_PATH_LESSONS = "/appdata/lessons/";
	private final static String STORYMAKER_DEFAULT_SERVER_URL = "https://storymaker.cc";
	
	 public void InitializeSQLCipher(String dbName, String passphrase) {
	        	      
		 File databaseFile = getDatabasePath(dbName);
	     databaseFile.mkdirs();
	     SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, passphrase, null);

	  }
	 
	 public static String initServerUrls (Context context)
	 {
		 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		 mBaseUrl = settings.getString("pserver", STORYMAKER_DEFAULT_SERVER_URL) ;
		 return mBaseUrl;
	 }
	 
//	public void InitializeSQLCipher(String dbName, String passphrase) {
//
//		File databaseFile = getDatabasePath(dbName);
//		databaseFile.mkdirs();
//		databaseFile.delete();
//		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, passphrase, null);
//		database.execSQL(StoryMakerDB.Schema.Projects.CREATE_TABLE_PROJECTS);
//		database.execSQL(StoryMakerDB.Schema.Lessons.CREATE_TABLE_LESSONS);
//		database.execSQL(StoryMakerDB.Schema.Medias.CREATE_TABLE_MEDIAS);
//		database.close();
//
//	}

	@Override
	public void onCreate() {
		super.onCreate();

		checkLocale ();
		
		SQLiteDatabase.loadLibs(this);

		initApp();
		 
	}
	
	private void initApp ()
	{
		try
		{
			initServerUrls(this);
	
		    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		    String customLessonLoc = settings.getString("plessonloc", null);
		    
		    String lessonUrlPath = mBaseUrl + URL_PATH_LESSONS + mLocale.getLanguage() + "/";
		    String lessonLocalPath = "lessons/" + mLocale.getLanguage();
		    
		    if (customLessonLoc != null)
		    {
		    	if (customLessonLoc.toLowerCase().startsWith("http"))
		    	{
		    		lessonUrlPath = customLessonLoc;
		    		lessonLocalPath = "lessons/" + lessonUrlPath.substring(lessonUrlPath.lastIndexOf('/')+1);
		    	}
		    	else
		    	{
		    		lessonUrlPath = mBaseUrl + URL_PATH_LESSONS + customLessonLoc + "/";
		    		lessonLocalPath = "lessons/" + customLessonLoc;
		    	}
		    }
	
	    	mLessonManager = new LessonManager (this, lessonUrlPath, new File(getExternalFilesDir(null), lessonLocalPath));
		    mServerManager = new ServerManager (getApplicationContext());
		}
		catch (Exception e)
		{
			Log.e(AppConstants.TAG,"error init app",e);
		}
	}

	public void updateLocale (String newLocale)
	{
        mLocale = new Locale(newLocale);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        settings.edit().putString(PREF_LOCALE,newLocale);
        settings.edit().commit();
        checkLocale();
        
        //need to reload lesson manager for new locale
        initServerUrls(this);

	}
	
	public static Locale getCurrentLocale ()
	{
		return mLocale;
	}
	
	 public boolean checkLocale ()
	    {
	        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

	        Configuration config = getResources().getConfiguration();

	        boolean useLangAr = settings.getBoolean("plocalear", false);
	        String lang = settings.getString(PREF_LOCALE, LOCALE_DEFAULT);
	        
	        if (useLangAr)
	        	lang = LANG_ARABIC;
	        
	        boolean updatedLocale = false;
	        
	        //if we have an arabic preference stored, then use it
	        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
	            mLocale = new Locale(lang);
	    		Locale.setDefault(mLocale);
	            config.locale = mLocale;
	            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
	            updatedLocale = true;
	        }
	        else if (Locale.getDefault().getLanguage().equalsIgnoreCase(LANG_ARABIC))
	        {
	        	//if device is default arabic, then switch the app to it
	        	  mLocale = Locale.getDefault();         
		            config.locale = mLocale;
		            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
		            updatedLocale = true;
	        }
	        
	        if (updatedLocale)
	        {
	            //need to reload lesson manager for new locale
				mLessonManager = new LessonManager (this, mBaseUrl + URL_PATH_LESSONS+ mLocale.getLanguage() + "/", new File(getExternalFilesDir(null), "lessons/" + mLocale.getLanguage()));

	        }
	        
	        return updatedLocale;
	    }
	
	public static ServerManager getServerManager ()
	{
		return mServerManager;
	}
	
	public static LessonManager getLessonManager ()
	{
		return mLessonManager;
	}
}
