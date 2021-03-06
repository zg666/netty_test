package server;



import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
/*
 * 利用DelimiterBasedFrameDecoder完成以分隔符作为码流结束的标识
 */
public class EchoServer {
	
	public void bind(int port) throws Exception{
		// 配置服务端的NIO线程组
		EventLoopGroup bossGroup =new NioEventLoopGroup();
		EventLoopGroup workerGroup =new NioEventLoopGroup();
	try {
		// 用于启动NIO服务器的辅助启动类，降低服务端的开发复杂度
		ServerBootstrap b=new ServerBootstrap();
		b.group(bossGroup,workerGroup).
		channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG,100)
		.handler(new LoggingHandler(LogLevel.INFO))
		.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception{
				ByteBuf delimiter=Unpooled.copiedBuffer("$_".getBytes());
				ch.pipeline().addLast(
						new DelimiterBasedFrameDecoder(1024,delimiter)); // 单条消息最大长度：1024
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new EchoServerHandler());
			}
			});
			
		
		// 绑定端口，同步等待成功
		ChannelFuture f=b.bind(port).sync();
		// 等待服务端监听端口关闭
		f.channel().closeFuture().sync();
	
		
	} catch (Exception e) {
		// TODO: handle exception
	}finally{
		// 优雅退出，释放线程池资源
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
	
	}

	
	public static void main(String[] args) throws Exception{
		int port=8080;
		if(args!=null&&args.length>0){
			try {
				port=Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}
		}
		new EchoServer().bind(port);
	}

}
