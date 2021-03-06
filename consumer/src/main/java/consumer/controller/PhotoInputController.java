package consumer.controller;


import aspect.DwqAnnotation;
import entity.User;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import service.PhotoInputService;
import util.StaticAddressUtil;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@DwqAnnotation
@CrossOrigin
public class PhotoInputController {

    @Autowired
    private PhotoInputService photoInputService;

    /**
     * @return 文件名
     * @throws IOException
     */
    @RequestMapping("/photo_uplode")
    @ResponseBody
    public JSONObject fileUp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream in = request.getInputStream();
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User user = (User) session.getAttribute("user");
        String text = request.getParameter("test_photo");
//	    String classPath = PhotoInputController.class.getClassLoader().getResource("").getPath();
//		String path = "E:/images/"+ user.getNickname() + "/";
        String path = StaticAddressUtil.bokeUploadImgTop + user.getId() + "/";
        Date date = new Date();
        String fileName = user.getId() + date.getTime() + ".jpg";
        String newPath = path + fileName;
        File myFile = new File(newPath);
        //判断目标文件所在的目录是否存在  
        if (!myFile.exists()) {
            myFile.mkdirs();
        }
        BufferedImage image = ImageIO.read(in);
        ImageIO.write(image, "jpg", myFile);

        in.close();
        try {
            return photoInputService.fileUp(fileName, user.getNickname(), user.getId(), text, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("/fileUpWriteImg")
    @ResponseBody
    public HashMap fileUpBaiduImg(HttpServletRequest request) throws IOException {
        HashMap map = new HashMap();
        MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = mRequest.getFileMap();
        for (Map.Entry entry : fileMap.entrySet()) {
            MultipartFile mFile = (MultipartFile) entry.getValue();
            Date date = new Date();
            String fileName = date.getTime() + "." + mFile.getOriginalFilename().split("\\.")[1];
            File file = new File(StaticAddressUtil.baiduImg + fileName);
            map.put("url", StaticAddressUtil.baiduImgUrl + fileName);
            if (!file.exists()) {//判断文件目录的存在
                file.mkdirs();
            }
            mFile.transferTo(file);
        }
        return map;
    }

    /**
     * 获取文件所在根目录地址
     * 在tomcat下的磁盘路径以项目名称结尾
     * 类似D:/tomcat7/webapps/STL-OEC/，
     * 区分windows和linux
     */
    public static String getRootFilePath() {
        String classPath = PhotoInputController.class.getClassLoader().getResource("").getPath();
        //windows-File.separator=\,classPath=/D:/MyWork/tomcat/tomcat7/webapps/STL-OEC/WEB-INF/classes/
        //rootPath=D:/MyWork/tomcat/tomcat7/webapps/STL-OEC
        //linux-rootPath=/,/root/Desktop/java/apache-tomcat-7.0.78/webapps/STL-OEC/WEB-INF/classes/
        String rootPath = "";

        //windows下
        if ("\\".equals(File.separator)) {
            rootPath = classPath.substring(1, classPath.indexOf("static"));
            //rootPath = rootPath.replace("/", "\\");
        }
        //linux下
        if ("/".equals(File.separator)) {
            rootPath = classPath.substring(0, classPath.indexOf("WEB-INF/classes"));
            //rootPath = rootPath.replace("\\", "/");
        }

        return rootPath;
    }

//	/**
//	 * 上传文件到给出的路径下
//	 * @param file 	上传的文件
//	 * @param path	上传到路径
//	 */
//	public static String uploadFile(MultipartFile file, String pathName) throws Exception{
//		String path = Tools.getRootFilePath(); 
//		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
//		String fileName = sdf.format(new Date()) + "_" + file.getOriginalFilename();
//		File targetFile = new File(path+"/"+pathName, fileName);  
//		if(!targetFile.exists()){  
//			targetFile.mkdirs();  
//		}
//		//保存  
//		file.transferTo(targetFile);
//		return file.getOriginalFilename()+","+fileName;
//	}


}
