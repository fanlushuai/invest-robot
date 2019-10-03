package name.auh.config;

import com.ulisesbocchio.jasyptspringboot.encryptor.SimplePBEStringEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PBEByteEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasypyConfig {

    /**
     * 来个空对象模式，利用一下已有的base64装饰器实现
     */
    class NUllEncryptor implements PBEByteEncryptor {

        @Override
        public byte[] encrypt(byte[] message) {
            return message;
        }

        @Override
        public byte[] decrypt(byte[] encryptedMessage) {
            return encryptedMessage;
        }

        @Override
        public void setPassword(String password) {

        }
    }


    /**
     * 加解密 使用base64
     *
     * @return
     */
    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        return new SimplePBEStringEncryptor(new NUllEncryptor());
    }
}
