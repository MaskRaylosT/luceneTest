package com.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class MainFangFa {
	//test
	private static String indexPath = "D:\\Myproject2\\LuceneTest\\save";
	private static final int maxlength=10000;
	public static IndexReader reader=null;
	public static Directory directory=null;
	public static Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_45);
    public static IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
			Version.LUCENE_45, new SmartChineseAnalyzer(Version.LUCENE_45));
    
    private static IndexSearcher indexSearcher;
	private static IndexWriter indexWriter;
	
    private static Object lock_r=new Object();
    private static Object lock_w=new Object();
    
    public static void indexSearcher(){
    	synchronized(lock_r){
			try {
				if (directory == null) {
					directory = FSDirectory.open(new File(indexPath));
				}
			} catch (Exception e) {
			}
			try {
				if(reader!=null){
					IndexReader reader1=IndexReader.open(directory);
					reader.close();	
					reader=reader1;
					indexSearcher = new IndexSearcher(reader);
				}else{
					reader=IndexReader.open(directory);
					indexSearcher = new IndexSearcher(reader);
				}
			} catch (Exception e) {
			}
    	}	
    }
    public static void indexWriter(){
    	synchronized(lock_w){
			try {
				if (directory == null) {
					directory = FSDirectory.open(new File(indexPath));
				}
			} catch (Exception e) {
			}
			try {
				if(indexWriter!=null){
					IndexWriter indexWriter1 = new IndexWriter(directory, indexWriterConfig);
					indexWriter.close();
					indexWriter=indexWriter1;
				}else{
					indexWriter = new IndexWriter(directory, indexWriterConfig);
				}
			} catch (Exception e) {
			}
    	}	
    }
	    
	public static void addText(String name,String auth,String text,String text2){
		Document doc = new Document();
		doc.add(new StringField("id", "4", Store.YES));
		doc.add(new StringField("name", name, Store.YES));
		doc.add(new StringField("auth", auth, Store.YES));
		doc.add(new StringField("text", text, Store.YES));
		doc.add(new StringField("text2", text2, Store.YES));
		try {
			indexWriter.addDocument(doc);
			indexWriter.commit();
			System.out.println("succeed");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("fail");
		}
		
	}
	
	public static void removeText(String id){
		
		try {
			Term term = new Term("id", id);
			indexWriter.deleteDocuments(term);
			indexWriter.commit();
			System.out.println("succeed");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("fail");
		}
		
	}

	public static void updateText(String id,String name,String auth,String text,String text2){
		try {
			Document doc = new Document();
			doc.add(new StringField("id", id, Store.YES));
			doc.add(new StringField("name", name, Store.YES));
			doc.add(new StringField("auth", auth, Store.YES));
			doc.add(new StringField("text", text, Store.YES));
			doc.add(new StringField("text2", text2, Store.YES));
			indexWriter.updateDocument(new Term("id", id), doc);
			indexWriter.commit();
			System.out.println("succeed");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("fail");
		}	
	}

	public static void readAll(){
		try {
			TopDocs topdocs = null;
			Term term = new Term("id", "*");
			WildcardQuery wildcardQuery = new WildcardQuery(term);
			topdocs=indexSearcher.search(wildcardQuery, maxlength);
			int needCounts = maxlength;
			if (needCounts > topdocs.totalHits) {
				needCounts = topdocs.totalHits;
			}
			for (int i = 0; i < needCounts; i++) {
				int docId = topdocs.scoreDocs[i].doc;
				Document document = indexSearcher.doc(docId);
				List<IndexableField> list=document.getFields();
				for(IndexableField item:list){
					String itemName=item.name();
					String itemvalue=item.stringValue();
					System.out.print(itemName+":"+itemvalue+" ");
				}	
				System.out.println("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void readTextById(String id){
		try {
			TopDocs topdocs = null;
			Term term = new Term("id", id);
			TermQuery termQuery = new TermQuery(term);
			BooleanQuery booleanQuery = new BooleanQuery();
			booleanQuery.add(termQuery, Occur.MUST);
			topdocs=indexSearcher.search(booleanQuery, maxlength);
			int needCounts = maxlength;
			if (needCounts > topdocs.totalHits) {
				needCounts = topdocs.totalHits;
			}
			for (int i = 0; i < needCounts; i++) {
				int docId = topdocs.scoreDocs[i].doc;
				Document document = indexSearcher.doc(docId);
				List<IndexableField> list=document.getFields();
				for(IndexableField item:list){
					String itemName=item.name();
					String itemvalue=item.stringValue();
					System.out.print(itemName+":"+itemvalue+" ");
				}	
				System.out.println("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void readTextByCondition(){
		try {
			//TermQuery 完全匹配
			//WildcardQuery 模糊匹配
			//BooleanQuery 多条件匹配
			TopDocs topdocs = null;
			BooleanQuery booleanQuery1 = new BooleanQuery();
			booleanQuery1.add(new TermQuery(new Term("auth", "3")), BooleanClause.Occur.SHOULD);
			booleanQuery1.add(new TermQuery(new Term("auth", "1")), BooleanClause.Occur.SHOULD);
			
			BooleanQuery booleanQuery = new BooleanQuery();
			booleanQuery.add(booleanQuery1,BooleanClause.Occur.MUST);
			WildcardQuery wildcardQuery = new WildcardQuery(new Term("name", "*"+"xiaoxianrou"+"*"));
			booleanQuery.add(wildcardQuery, BooleanClause.Occur.MUST);
			topdocs=indexSearcher.search(booleanQuery, maxlength);
			int needCounts = maxlength;
			if (needCounts > topdocs.totalHits) {
				needCounts = topdocs.totalHits;
			}
			for (int i = 0; i < needCounts; i++) {
				int docId = topdocs.scoreDocs[i].doc;
				Document document = indexSearcher.doc(docId);
				List<IndexableField> list=document.getFields();
				for(IndexableField item:list){
					String itemName=item.name();
					String itemvalue=item.stringValue();
					System.out.print(itemName+":"+itemvalue+" ");
				}	
				System.out.println("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		indexWriter();
		indexSearcher();
//		addText("xiaoxianrou","2","中文111","English222");
//		updateText("3","xiaoxianrou","3","中文222","English111");
//		removeText("4");
//		readAll();
//		readTextById("3");
		readTextByCondition();
		
	}
	
}
