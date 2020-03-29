import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableFile{
	
	public SerializableFile() {
		
	}
	
	static Object load(String pathAndFilename) {
		
		// Deserialization 
		Object obj = null;
        try
        {    
            // Reading the object from a file 
            FileInputStream file = new FileInputStream(pathAndFilename); 
            ObjectInputStream in = new ObjectInputStream(file); 
              
            // Method for deserialization of object 
            obj = in.readObject(); 
              
            in.close(); 
            file.close(); 
              
            System.out.println("Object has been deserialized "); 
            return obj;
        } 
          
        catch(IOException ex) 
        { 
            System.out.println("IOException is caught"); 
        } 
          
        catch(ClassNotFoundException ex) 
        { 
            System.out.println("ClassNotFoundException is caught"); 
        } 
        
        return null;
  
	}
	
	static void save(String pathAndFilename, Object obj) {

		// Saving of object in a file
		FileOutputStream file = null;
		try {
			file = new FileOutputStream(pathAndFilename);
		
			ObjectOutputStream out = new ObjectOutputStream(file);
			// Method for serialization of object
			out.writeObject(obj);

			out.close();
			file.close();
		} 
		
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	
	
}