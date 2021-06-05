
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class SVGTest {
	TranscodingHints transcoderHints;
	String css;
	//JSVGCanvas canvas;

	public SVGTest() {
		// Rendering hints can't be set programatically, so
		// we override defaults with a temporary stylesheet.
		// These defaults emphasize quality and precision, and
		// are more similar to the defaults of other SVG viewers.
		// SVG documents can still override these defaults.
		//canvas = new JSVGCanvas();
		//canvas.setDocumentState(JSVGCanvas.ALWAYS_INTERACTIVE);

		css = "svg {" + "shape-rendering: geometricPrecision;" + "text-rendering:  geometricPrecision;"
				+ "color-rendering: optimizeQuality;" + "image-rendering: optimizeQuality;" +

				"}";

		File cssFile = null;
		try {
			cssFile = File.createTempFile("batik-default-override-", ".css");
			FileUtils.writeStringToFile(cssFile, css, Charset.forName("UTF-8"));

			transcoderHints = new TranscodingHints();
			transcoderHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
			transcoderHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, SVGDOMImplementation.getDOMImplementation());
			transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
			transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
			transcoderHints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toURI().toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// cssFile.delete();
		}

	}

	public SVGDocument getSVGDocument(String pathAndName) {
		File file = new File(pathAndName);
		String uri = file.toURI().toString();

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
		SVGDocument doc = null;
		try {
			doc = factory.createSVGDocument(uri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("SVGDocument getSVGDocument load fail ");
			return null;
		}

		String strDocWidth = doc.getRootElement().getAttribute("width");
		String strDocHeight = doc.getRootElement().getAttribute("height");
		System.out.println("SVGDocument getSVGDocument loaded is " + strDocWidth + " " + strDocHeight);
		//canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		//canvas.setDocument(doc);

		return doc;
	}

		/*
	public  void saveSvgDocument(String pathAndName, SVGDocument document)	
	{
		File file = new File(pathAndName);
	    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
	    Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			svgGenerator.stream(out,false);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	    catch (SVGGraphics2DIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	*/

	void setStrokeWeight(SVGDocument doc) {
		SVGSVGElement root = doc.getRootElement();
		root.setAttributeNS(null,"vector-effect", "non-scaling-stoke");
		
	}
	
	
	void createChangedFile(String inputPathAndName, String outputPathAndName) {
		
		
		
		File inputFile= new File(inputPathAndName);
		File outputFile = new File(outputPathAndName);
		
		
	    
		
		String search = "stroke-width:*;";
		String replace = "stroke-width:56;";

		try{
		    FileReader fr = new FileReader(inputFile);
		    String s;
		    String totalStr = "";
		    try (BufferedReader br = new BufferedReader(fr)) {

		        while ((s = br.readLine()) != null) {
		            totalStr += s;
		        }
		        totalStr = totalStr.replaceAll(search, replace);
		        
		        
		        FileWriter fw = new FileWriter(outputFile);
		    fw.write(totalStr);
		    fw.close();
		    }
		}catch(Exception e){
		    e.printStackTrace();
		}
		
		
		
	}
	
	
	BufferedImage svgToBufferedImage(SVGDocument svgd, int w, int h) {
		//SVGDocument svgd = canvas.getSVGDocument();
		TranscoderInput input = new TranscoderInput(svgd);

		Float width = (float) w;
		Float height = (float) h;
		transcoderHints.put(ImageTranscoder.KEY_WIDTH, width);
		transcoderHints.put(ImageTranscoder.KEY_HEIGHT, height);

		BufferedImageTranscoder t = null;
		try {

			t = new BufferedImageTranscoder();

			t.setTranscodingHints(transcoderHints);

			t.transcode(input, null);

		} catch (TranscoderException e) {
			// TODO Auto-generated catch block
			System.out.println("svgToBufferedImage:transcoder exception");
			e.printStackTrace();
			return null;
		}
		return t.getImage();
	}

	BufferedImage svgToBufferedImage(String pathAndName, int w, int h) {
		File svgFile = new File(pathAndName);
		System.out.println("here1");
		Float width = (float) w;
		Float height = (float) h;
		transcoderHints.put(ImageTranscoder.KEY_WIDTH, width);
		transcoderHints.put(ImageTranscoder.KEY_HEIGHT, height);

		TranscoderInput input;
		BufferedImageTranscoder t = null;
		try {

			input = new TranscoderInput(new FileInputStream(svgFile));

			t = new BufferedImageTranscoder();

			t.setTranscodingHints(transcoderHints);

			t.transcode(input, null);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("svgToBufferedImage:file not found");
			e.printStackTrace();
		} catch (TranscoderException e) {
			// TODO Auto-generated catch block
			System.out.println("svgToBufferedImage:transcoder exception");
			e.printStackTrace();
			return null;
		}
		return t.getImage();
	}

}

class BufferedImageTranscoder extends ImageTranscoder {
	BufferedImage bufferedImage;

	@Override
	public BufferedImage createImage(int w, int h) {
		System.out.println("BufferedImageTranscoder " + w + " " + h);
		return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	public void writeImage(BufferedImage image, TranscoderOutput out) throws TranscoderException {
		bufferedImage = image;
	}

	BufferedImage getImage() {
		return bufferedImage;
	}

}

//////////////////////////////////////////////////////////////////////

class BlankRunnable implements Runnable {

	public void run() {
		// Element bar = doc.getElementById(name);
		// if (bar == null) {
		// return;
		// }

	}
}

