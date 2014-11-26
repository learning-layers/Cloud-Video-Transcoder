package de.dbis.mpeg7;

public class sevianno {
	
	public static void addMediaDescription(String videoUri, String thumbnailUri, String title, String uploader) {
		
		LasConnection lasConnection= LasConnection.getConnection();
		
		String result = lasConnection.connect("sevianno","sevianno");
		if(result.equals(LasConnection.AUTHENTICATION_PROBLEM)){
			return; 
		}
		
		String[] semanticReferences = {};
		String[] keywords = new String[]{"ClViTra"};
		Object [] addMediaDescriptionParams = {"ClViTra",uploader,thumbnailUri,videoUri,"ClViTra",keywords,semanticReferences};
		Object [] existsMediaDescriptionParams = {videoUri};
		Object [] setMediaCreationTitleParams = {videoUri,title};
		
		
		
		if(!lasConnection.existsMediaDescription(existsMediaDescriptionParams)){
			result = lasConnection.addMediaDescription(addMediaDescriptionParams);
			if(result == null){
				return;
			}
		
			lasConnection.setMediaCreationTitle(setMediaCreationTitleParams);	
		}
		
		lasConnection.disconnect();
		
	}

}
