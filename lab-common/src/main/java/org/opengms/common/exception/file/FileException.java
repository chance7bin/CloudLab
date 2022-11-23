package org.opengms.common.exception.file;


import org.opengms.common.exception.base.BaseException;

/**
 * 文件信息异常类
 * 
 * @author 7bin
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
