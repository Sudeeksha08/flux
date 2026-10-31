package com.brightloop.invoice.service;
import com.brightloop.invoice.dto.InvoicePdfView;
import com.brightloop.invoice.dto.InvoiceResponse;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Locale;
@Service public class InvoicePdfService{
 public byte[] render(InvoiceResponse invoice){try(PDDocument doc=new PDDocument();ByteArrayOutputStream out=new ByteArrayOutputStream()){PDType1Font font=new PDType1Font(Standard14Fonts.FontName.HELVETICA);PDType1Font bold=new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);PDPage page=new PDPage(PDRectangle.LETTER);doc.addPage(page);PDPageContentStream stream=new PDPageContentStream(doc,page);float y=740;stream.setFont(bold,18);write(stream,"Brightloop Invoice",50,y);y-=28;stream.setFont(font,10);write(stream,"Invoice: "+invoice.number(),50,y);write(stream,"Date: "+invoice.invoiceDate()+"     Due: "+invoice.dueDate(),350,y);y-=18;write(stream,"Bill to: "+invoice.customerName(),50,y);y-=14;write(stream,invoice.customerEmail(),50,y);y-=14;write(stream,invoice.customerAddress()==null?"":invoice.customerAddress(),50,y);y-=28;stream.setFont(bold,10);write(stream,"Item",50,y);write(stream,"Qty",360,y);write(stream,"Unit (INR)",405,y);write(stream,"Total (INR)",500,y);y-=12;stream.setFont(font,9);int index=0;for(var item:invoice.items()){if(y<80){stream.close();page=new PDPage(PDRectangle.LETTER);doc.addPage(page);stream=new PDPageContentStream(doc,page);y=740;stream.setFont(font,9);}String name=item.productName()+(item.variantName()==null?"":" / "+item.variantName());write(stream,fit(name,45),50,y);write(stream,Integer.toString(item.quantity()),360,y);write(stream,fmt(item.unitPrice()),405,y);write(stream,fmt(item.totalAmount()),500,y);y-=14;index++;}y-=12;stream.setFont(font,10);write(stream,"Subtotal (INR): "+fmt(invoice.subtotal()),360,y);y-=15;write(stream,"Discount (INR): "+fmt(invoice.discountAmount()),360,y);y-=15;write(stream,"Tax (INR): "+fmt(invoice.taxAmount()),360,y);y-=17;stream.setFont(bold,11);write(stream,"Total (INR): "+fmt(invoice.totalAmount()),360,y);y-=18;stream.setFont(font,10);write(stream,"Paid (INR): "+fmt(invoice.paidAmount()),360,y);y-=15;write(stream,"Outstanding (INR): "+fmt(invoice.outstandingAmount()),360,y);stream.close();doc.save(out);return out.toByteArray();}catch(IOException e){throw new IllegalStateException("Could not render invoice PDF",e);}}
 private void write(PDPageContentStream s,String text,float x,float y)throws IOException{s.beginText();s.newLineAtOffset(x,y);s.showText(ascii(text));s.endText();}
 private String fit(String s,int max){return s.length()>max?s.substring(0,max-3)+"...":s;}
 private String ascii(String s){return s==null?"":s.replaceAll("[^\\x20-\\x7E]","?");}
 private String fmt(java.math.BigDecimal amount){return amount.setScale(2,java.math.RoundingMode.HALF_UP).toPlainString();}
}
