# kotlin-spring-boot-google_directions_api-start

## Description

C.T.R. - Car Travel Route API - Projeto baseado no conteudo do livro *"APIs REST em Kotlin: Seus serviços prontos para o mundo real"*

## Tecnologies

Kotlin, Spring boot 2.7.4, Google Directions API

## Future Tasks

    [ ] Adicionar localização do motorista
    [ ] Permitir o motorista confirmar viagem
    [ ] Informar ao passageiro o motorista confirmado para a viagem
    [ ] Confirmar início de viagem
    [ ] Confirmar fim da viagem
    [ ] Salvar o registro da viagem

## Related Future Projects

    [ ] Administration Interface (Vue)
    [ ] Mobile Interface (Flutter)

## How to start

1. Generate key

        keytool -genkeypair -alias ctr -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650 -storepass 12345678

2. Move keystore.p12 file to "ctr-backend/src/main/resources" folder

        mv keystore.p12 ctr-backend/src/main/resources

3. Configure docker-composer-prod.yml file with google api key

        GOOGLE_API_KEY=*YOUR API KEY*

4. If on linux, run the script start services

        chmod +x runAll.sh &&  ./runAll.sh

5. Open the browser and the managers:    

    * PgAdmin 4
        * http://localhost:5001
        * user: guest@guest.com
        * password: 12345678
    * Swagger API Docs
        * http://localhost:8080/swagger-ui.html
        
## Credits

[Link github do livro](https://github.com/alesaudate/rest-kotlin)

## License

MIT