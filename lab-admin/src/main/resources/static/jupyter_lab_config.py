# Configuration file for lab.

c = get_config()  # noqa

# 设置允许远程访问
c.ServerApp.allow_origin = '*'

# 是否允许用户远程访问
c.ServerApp.allow_remote_access = True

# 是否允许用户作为root用户使用服务器
c.ServerApp.allow_root = True

# 设置启动后的jupyterlab的文件根目录
c.ServerApp.root_dir = '/opt/notebooks'

# jupyter服务器监听ip
c.ServerApp.ip = '*'

# jupyter服务器监听端口
c.ServerApp.port = 8888

# 嵌入iframe框设置
c.ServerApp.tornado_settings = { 'headers': { 'Content-Security-Policy': "frame-ancestors self *" } }

# 设置token,通过代码为每个用户生成唯一token作为jupyter的认证
# c.ServerApp.token = 'xxx'
