[
    customers: [
        C1: [
            address: [
                city: Springfield
                country: Switzerland
                street: "Street 1"
                zip: 1234
            ]
            credit: 1000
            name: "Customer One"
        ]
        C2: [
            address: [
                city: Springfield
                country: Switzerland
                street: "Street 2"
                zip: 1234
            ]
            credit: 100
            name: "Customer Two"
        ]
    ]
    products: [
        P1: [
            name: "Product 1"
            price: 123
        ]
        P2: [
            name: "Product 1"
            price: 123
        ]
    ]
    urls: [
        Binary1: @StaticBinary [
            Body: 0x41424241
            ContentType: "text/plain"
        ]
        Dynamic1: @Dynamic @Func -> @Function [
            args: [
                @name ref
                @name props
                @name params
                @name headers
                @name cookies
            ]
            body: @Block [
                @Return @Complex [
                    [
                        key: @Constant StaticHtml
                        val: @Complex [
                            [
                                key: @Constant ContentType
                                val: @Constant "text/html"
                            ]
                            [
                                key: @Constant Body
                                val: @Complex [
                                    [
                                        key: @Constant html
                                        val: @Complex [
                                            [
                                                key: @Constant 1
                                                val: @Constant @head [
                                                    @title "This is the title"
                                                ]
                                            ]
                                            [
                                                key: @Constant 2
                                                val: @Complex [
                                                    [
                                                        key: @Constant body
                                                        val: @Complex [
                                                            [
                                                                key: @Constant 1
                                                                val: @Complex [
                                                                    [
                                                                        key: @Constant p
                                                                        val: @Concat [
                                                                            expr1: @Concat [
                                                                                expr1: @Constant "Hello, user "
                                                                                expr2: @ReferenceContent @Constant [/users/philip/name]
                                                                            ]
                                                                            expr2: @Constant "!"
                                                                        ]
                                                                    ]
                                                                ]
                                                            ]
                                                            [
                                                                key: @Constant 2
                                                                val: @Complex [
                                                                    [
                                                                        key: @Constant p
                                                                        val: @Concat [
                                                                            expr1: @Concat [
                                                                                expr1: @Concat [
                                                                                    expr1: @Concat [
                                                                                        expr1: @Constant "The current time is "
                                                                                        expr2: @Call [
                                                                                            args: [ ]
                                                                                            func: @Constant -> @CompiledFunction time_current_text
                                                                                        ]
                                                                                    ]
                                                                                    expr2: @Constant " and you are from "
                                                                                ]
                                                                                expr2: @Child [
                                                                                    expr1: @Variable props
                                                                                    expr2: @Constant remote_host
                                                                                ]
                                                                            ]
                                                                            expr2: @Constant "."
                                                                        ]
                                                                    ]
                                                                ]
                                                            ]
                                                        ]
                                                    ]
                                                ]
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
        Dynamic2: @Dynamic @Func -> @Function [
            args: [
                @name ref
                @name props
                @name params
                @name headers
                @name cookies
            ]
            body: @Block [
                @Return @Complex [
                    [
                        key: @Constant StaticHtml
                        val: @Complex [
                            [
                                key: @Constant ContentType
                                val: @Constant "text/html"
                            ]
                            [
                                key: @Constant Body
                                val: @Complex [
                                    [
                                        key: @Constant html
                                        val: @Complex [
                                            [
                                                key: @Constant 1
                                                val: @Constant @head [
                                                    @title "HTTP request test"
                                                    @style "p { font-weight: bold}"
                                                ]
                                            ]
                                            [
                                                key: @Constant 2
                                                val: @Complex [
                                                    [
                                                        key: @Constant body
                                                        val: @Complex [
                                                            [
                                                                key: @Constant 1
                                                                val: @Constant @h1 "HTTP request test"
                                                            ]
                                                            [
                                                                key: @Constant 2
                                                                val: @Constant @p "Reference:"
                                                            ]
                                                            [
                                                                key: @Constant 3
                                                                val: @Complex [
                                                                    [
                                                                        key: @Constant pre
                                                                        val: @Call [
                                                                            args: [
                                                                                @Variable ref
                                                                                @Constant true
                                                                            ]
                                                                            func: @Constant -> @CompiledFunction session_serialize
                                                                        ]
                                                                    ]
                                                                ]
                                                            ]
                                                            [
                                                                key: @Constant 4
                                                                val: @Constant @p "Request properties:"
                                                            ]
                                                            [
                                                                key: @Constant 5
                                                                val: @Complex [
                                                                    [
                                                                        key: @Constant pre
                                                                        val: @Call [
                                                                            args: [
                                                                                @Variable props
                                                                                @Constant true
                                                                            ]
                                                                            func: @Constant -> @CompiledFunction session_serialize
                                                                        ]
                                                                    ]
                                                                ]
                                                            ]
                                                            [
                                                                key: @Constant 6
                                                                val: @Constant @p "Request parameters:"
                                                            ]
                                                            [
                                                                key: @Constant 7
                                                                val: @Complex [
                                                                    [
                                                                        key: @Constant pre
                                                                        val: @Call [
                                                                            args: [
                                                                                @Variable params
                                                                                @Constant true
                                                                            ]
                                                                            func: @Constant -> @CompiledFunction session_serialize
                                                                        ]
                                                                    ]
                                                                ]
                                                            ]
                                                            [
                                                                key: @Constant 8
                                                                val: @Constant @p "Request headers:"
                                                            ]
                                                            [
                                                                key: @Constant 9
                                                                val: @Complex [
                                                                    [
                                                                        key: @Constant pre
                                                                        val: @Call [
                                                                            args: [
                                                                                @Variable headers
                                                                                @Constant true
                                                                            ]
                                                                            func: @Constant -> @CompiledFunction session_serialize
                                                                        ]
                                                                    ]
                                                                ]
                                                            ]
                                                            [
                                                                key: @Constant 10
                                                                val: @Constant @p "Request cookies:"
                                                            ]
                                                            [
                                                                key: @Constant 11
                                                                val: @Complex [
                                                                    [
                                                                        key: @Constant pre
                                                                        val: @Call [
                                                                            args: [
                                                                                @Variable cookies
                                                                                @Constant true
                                                                            ]
                                                                            func: @Constant -> @CompiledFunction session_serialize
                                                                        ]
                                                                    ]
                                                                ]
                                                            ]
                                                        ]
                                                    ]
                                                ]
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                            [
                                key: @Constant Headers
                                val: @Constant [
                                    [
                                        "X-STYX-Header1": "Value 1"
                                    ]
                                    [
                                        "X-STYX-Header2": "Value 2"
                                    ]
                                ]
                            ]
                            [
                                key: @Constant Cookies
                                val: @Complex [
                                    [
                                        key: @Constant 1
                                        val: @Complex [
                                            [
                                                key: @Constant name
                                                val: @Constant MyCookieName
                                            ]
                                            [
                                                key: @Constant value
                                                val: @Concat [
                                                    expr1: @Concat [
                                                        expr1: @Constant "MyCookieValue("
                                                        expr2: @Call [
                                                            args: [ ]
                                                            func: @Constant -> @CompiledFunction time_current_millis
                                                        ]
                                                    ]
                                                    expr2: @Constant ")"
                                                ]
                                            ]
                                            [
                                                key: @Constant comment
                                                val: @Constant ThisIsAComment
                                            ]
                                            [
                                                key: @Constant max_age
                                                val: @Constant 120
                                            ]
                                            [
                                                key: @Constant http_only
                                                val: @Constant true
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
        Html1: @StaticHtml [
            Body: @html [
                @head [
                    @title "This is the title"
                ]
                @body [
                    @h1 "This is the heading"
                    @p "This is a paragraph."
                    @p [
                        "This is another "
                        "longer paragraph."
                    ]
                    @p [
                        "This is a paragraph with "
                        @u mixed
                        " content."
                    ]
                    @p [
                        "This is a paragraph with "
                        @span [
                            [
                                "#style": "color: red"
                            ]
                            red
                        ]
                        " content."
                    ]
                ]
            ]
            ContentType: "text/html"
        ]
        Html2: @StaticHtml [
            Body: @html [
                @head [
                    @title "Welcome to STYX"
                ]
                @body [
                    @h1 "Welcome to STYX"
                    @p "STYX is enabled by:"
                    @table [
                        @tr [
                            @td @img [
                                "#src": JavaLogo
                            ]
                            @td [
                                @a [
                                    [
                                        "#href": "http://en.wikipedia.org/wiki/Java_(programming_language)"
                                    ]
                                    Java
                                ]
                                " is, as of 2014, one of the most popular programming languages in use, particularly for client-server web applications, with a reported 9 million developers. "
                                "Java was originally developed by James Gosling at Sun Microsystems (which has since merged into Oracle Corporation) and released in 1995 as a core component of Sun Microsystems' Java platform."
                            ]
                        ]
                        @tr [
                            @td @img [
                                "#src": SqliteLogo
                            ]
                            @td [
                                @a [
                                    [
                                        "#href": "http://en.wikipedia.org/wiki/SQLite"
                                    ]
                                    SQLite
                                ]
                                " is a software library that implements a self-contained, serverless, zero-configuration, transactional SQL database engine. "
                                "SQLite is the most widely deployed SQL database engine in the world. The source code for SQLite is in the public domain."
                            ]
                        ]
                    ]
                ]
            ]
            ContentType: "text/html"
        ]
        JavaLogo: @StaticBinary [
            Body: 0x89504E470D0A1A0A0000000D494844520000004D0000009208060000000061474A00000006624B474400FF00FF00FFA0BDA793000018C249444154789CED9D7B7CD4D599FFDFCFF94E92C924DCC14C0228058A085AAFEDEA5657D4FEAC48815A1B8BB5D4EDB69522EAD65D950012BE2428A0BB767FC855F7557FDADA5717EA85205A2FEBD2DA566DB5D62AAE5AE8D20AB970934B32B97ECFB37F4C126626DF995C984CC24F3EAFD7BC92EF736ECF7CE69CF33D97E73C074E70D42D0D4FCD749926D305A61B82B9EC6879784A26CB3CE149B3D8ED46655126CB3CE14913C33EE08A86F2519FCE5499273C698A5840AC7AD33355E6094F5A1BAC7041A6CA3AE14913D5B1004629CC5499273C69281301542598A9224F7CD2442E8EFED5C3992AF28426EDA81B9E049C1B7DD23F65AADC139A34C7C8ECB6FF55CDEF3355EE094B5A6459D16885EFB73EB6A87A5B3355F6094B9A5AEB024100417F31C0ADD99BA9B24F48D2EACA0BBE04F20FED029535992C5F3259583A105956345AADFE0E286815BD1D5A5C75AE089A291D4EA89A7668F9A943D4EA731C230C5417659230388148DBB772F880ACE6A6ADC0E43699088FE72DA9CED80BA00D814C17D813EC5B397C40A831EB39E0A21871758B63FEB12FF4E9F7A445968D1AA98DDE66E0FC18719331529CB770CF81BED0A95F37CFA3E5E1296ABDB789274C45F51BB97757FEAAAFF4EAB7352D5256344B557F08E4C6CA05568696546FEA6E7E47DDF024C79189B95ECB0BE2EEAB3D1EDDFADD90435D4C440ACB101692A09FA8DC1F5A52795757F33A525E74BA839D85CA7444378706E5DF27B7ED683C5E1DFB554DABBDE79482BA1667A3087FD7215065696849A5DB591EBA81ACBA9AF0F582DC86EAF9203F13E3CC0CDDBD7B4FBAF4EC3735ADB6ACE02C419E04199F10A4C0DD79A555F7A64A7F70C59041C1E6E03FA8CA3CD07140B522FF9C5F5AF99374EBDA2F488B2C2DBC4685C780FC84A07A51BD31551FA66E51A8CE709BA07702435BC5FFDE6473EE1CE2EE3AD41BFAF6396975E5E179A8FC5FC04908AA16A333437757FF3659DADAB2C26F08721F68DB52F77E54BE93B7A47273AF294C1F92A68A44CA0B97010B7D82B71BC3B4DCBBABFEE297B6C11D35DE9A96F58A5C1193DF2F25D03C2B6FD1FEAADED2B90D7D429ABA0422A6E861D0BF4F0C13E4E5469B7D6DB2A655B7B46826C2A3A0836252AD0D15547E5FE6D0DC6B4AC720E3835B5524E214AEF7230C7832D70E999A823017D1A762085381DBF34A2BE7658A30E8832147A4BCC805FDB64FD00B213BF47A71B737F9A62B0BAF5434768C6641BE1B2AADFC61AF289A02196D9E754BC35311D9EA53EEEF42B9F632B9B3A6CE375D7978092A6EAC4C91F9F9A595F7F592AA299131D28EBA05A71863DE054624A870D888776EEEE29AFFF14DB7347C9911798998AE4445FF237F71F5ACDED4371532D6A719C72CA30361A0E82DC9083BB862C82023F228F17A1EB28E33AF97D4EC123242DAC1154306A1CCF6097A2B6F71D5E3C9D2E534056F0546C7CA141E1BD8474B426DC80869C1C6E0E5B4EE1CC542947B922D55AB22C0773AA4117D3EFD1A760F1921CD8A9CEE23AECDD59CA44BD5B54B0B2603A725CAD5732AD3A95B4F9011D204F51BDAFC52DC5D0D49D38839DB37C0D153D2A5574F91A1178174AC1DCA472953181DEC2B57BD344D4AF51819214DF17E972813E448CA34D6449204DDA8ABC6E7A445B11E2223A4E597D6BC03B23356A68694CDCC11FBE7244123EB0E456E4F9B723D40C6C66982AE8E13589D9C242A00C1FCC06F01DFA569411745968D1A993EEDBA878C91966BE521D0637D9B70DED17BC31D06BBEDC1FFB4BB1ED89224385FAD5D9F6615BB8CCCD534B732A22A25B1653B2DE237E06D8715BB2E79A87EA9AEACF0A634A9D72D647C3DADAEBC6823AAC5D127DD11B2D567884B4BB2F891B2A29715BD2C59768E78670717EFDD9924BC5790F1F5B4C6ACFAEF02AD2BB2323EE284E7A48A6FAC7C0FA84F129CE7A9B32AAD0A760119276D68C9C78705B901880E6C554A8FB845C393C50FBA7B3E54A13C4596571F2D0BA77CA9A41B7D6296102AADFCB588CE062C708A31F6C154F1F3BCAA95A0C95E0A186466BA754C853EB3E5082DAEFE9908770008322BB2B4F09A6471C5C5366637CE06DDE11F81B1BDA4A62FFAD40026B4B8EA07A89403A8B0F6C8BD2387258B3BB4E4E3C362B8017C5645AC249DC3F606FADC6A286F496529481910369E97D27636BA072A7E2B236FF58E76FEE873D200F24A2B9720EA8ACAD72265E1EB3A899EB8115CE765C9D3BDA59B1FFA056900798BAB972A325F91D5A9DEA688571DF7ACACCAF44A6EBF210D20BABB240B1D47931ABBA8CAD0634FF27A4887BA19502D0E7D6ECBE187C8D2C26B44E5CD5CB7F2AF8961754B8BD6223A17B4D218F9DB64A60B5DC1B492B56303C2A528A72B0C11E58815DDAE8E79F1997BE62635CD4A3B69535C37302432E2D39E913345A4D0A2C3C44A35863FE6E40CFBDD26F73ADFCDE044A84B20717AA5EE88FC8809EC12D82F862FF694B0990BD64F52B50F8A724485BDC048E0728E595D5A81973CCFDCF2CCFDDFEB7050EDB849735DD7BC553FE24215F37F149D22702120A23C678D3EE3B5B0F5D9FBE755779A511710292B5CAE708167E5FA816EE5FE9EE6F3E5927563B2760EFB68D3A6EBBC3659F1FC0D839AF0FE51A316986D8B9C1184EB2B96DF5C119BBEC7A44D9FBFE66211B9019809EDA77AB7ABB221D894FDF8A61F7CE7604FF3F6437D59E125162E094DAC5A29D7E1759EA267F8D282F51719B53F0706B68A1A517B65C5CA5B7ED916A75BA415DFFE406E634EEEF5A8DE8A704E4CD00E41EE3A3758B3D9755D9B06DDE3A0EEE4EC4856CDB04C985101CC28595B0C6C8CD1E0FDF0C1C0671E7A684E337483B4990BD65EA2CAFF838E531647ECE94F2DBFE5C3E3D6360D282EDEE8348F3B305AD1F1169D202293140DA03C190C8ED8D6D53E7546C9DA77893D1DA372D5E695739F872E5A0D4D773784B4C17B019F0D5F004FCDA333E7AF7D50D5DB5671DFADBDBA2F39F5D6553939C19C51D6348F0473AA0A238D305295B1A0E31AD93F16C80610E40056CB83B923D67595AC3628F28210B3242F7A19D075D2B6B8732233E7AF5BAAA20B81013E512E54E142C46146C9DA5AD0DD20FB50A9416CB5A8ECB3E2BFDE1FFD729A2748B62241D05CD08020831406014388F62F835A3F39168FB621A600DA3E1B1555781FE53531FAAAC969DAF4B47B7B8FEC6E0DF680C6354469FFDE5DB64FDBBC72EE8A69256B370454A7ABC897889E22F9141D9B783EC8446022A280A0D2593FD0669BA009CF4961813D0A3B0DF29EA2EFA0F63D93DBFCC79E929408151919AB848AB6BFD8BA65D4B775C5CD1F038FB57E98EAAE1A1868704E47B508CCA9C610566538CA7084611CB3D6CE07B25004217113B816A109E590A04D8AD481D48136A9E841D4EC47743FC801D4D6A8E7FC4F6EDED0BF74B7B97517AA4C91B8677DA5EDFF7E3923E86BCC58B8E632ACBC1C23DA113EE84C6A7B7BF6ABB9677FC04D376DC8C2CA0F6265AAB2A88D30E845D2AE5EB4BA83C5CF8980EAA1DE3D40BBF18D206BB7AC9CBB31364E5A0D958B8B373A4DE3F67F4B45E788756E4C67DE99C0CC05EB6E53D53B6344158783351D0EE2A6AD4F9B3E7FF56411F308F059E0F58A15375F98AEBC3381190BD6DC85CA725A5B9FA2AB823B47FC53ECFCB40D69216D5AC9DA2146D821DA7E36C9036EC9090EFF61EFBCE554BE5CB2FE348B9DA8C299626522C26420ACAAB3B7AC9CD7E503B4C5EEC6ECC686FD0F026DBBF57B11EEAA587EF3A3C9D2A485B4D65AF6AE4FD01141B783F900D5FD56D82FC83E55EF00C8818072A0C988FFA0D748C8787630C60C46EDA90639D50AA3059D80CA443A1E3E6B4333E88A9C20F76D72E7A53C0CDBBA44F463A2FE8A9A401FCCD140F9A69573523A7B4A5BF39C51B2C6055944FF3843DA02BC6490B94FAF98BB2B31F0A69B3664D50C6DB945917B8008F09075644DAA85C758A4759C36A364DDB982DEAED1E5A2819D26481F2282BEAD2AAF0BFA7253AEF78BE7DCDB7C8D06672C58F3155559264A35C24F35E8FC788B3B279901A12F7A65703BC57D2438A8A1FE62E00BA07FA7D183AF49CDAABA817A819D283B5474A7287F6C71CCEF437F1AF6DF7E1D7622662E58FB2DABAA01D12D4F2DBFA5C79B31199B114C75570DCCAE97D3D431C3C5720A2AC3AD902F4850D06387FB9588151A05A953EC0135BAD760F6D1E2EDEFED1594933889FE855E6D9EC5F3370C6A34DEA59AE3BCD4DDCEB63FA357860757DE717F5E6E206F7123DEF7D552B6C59D53D179AA130769AF6937DDB421AB7A887D0DD1F35A453B447441760E3FEF6CB0D95514DFFEEF431BB21B2789C8245586D3E23CB4E55FE7F4784BAFBB483B69D317AE3E4FAC79D32748517689F027852A11F6584BB511D9ED895407D4C64DB73C4B16620B04A7488D86C54A21E8F8D6E9D22909391F52B85F34E7C18AFBBE7D34DDDF29116927ADB878A3D3386EFFAB4427EE99C601511E31C63EDC9BBB63695F4FDBB4E93A2FA731FB2A157E829F015E6F42711009795EA0576723BDFAF69CB960FD248B779BA85C0B24379F3A3EFC15D886CA734772834F6F73BFD5EB5691DDDB61773766D7371E3CDF583B0EA150C1081CC67A15A946EB535C3790DF34FC5263E51290F341CE011DD5037DF78BCADB8ABE83E10F9EF2CAD61537273B43D56BE894B46B16AC9EE0215F06B91CE512A24DEE55D05F836C57B5EF89E6FEB5BB1D70F1ED0FE43664678F35C85815090A0CD284EE42D1A38854063CDDDB64A4BA7537AC5771F5A2D5A7059BADF7E47DB7ED4E16C797B4A9B7AECA09E4395F01B949E0524011FD4F541ECE090EDFDCDBDB677D09D775CD5B0DA7FCD9363B17241BC6240C6E55A62F58FF0D515D4ED4660BE04F6AECAC2DF7DE9231DFD87D89371B467C41E03493ED9D01BCE217A7BDA65D79C7FD79C140FEE3A0F1071944AFAD583EEFC9DE55B5EF31DDDD1032F5F636152D053655AC98FBF7444D043AA09DB4E9256B7F2AF0B58E51B406C475C46E3A9E35A8FE88190B1F2E506DBE48E04A94AF038D8A2EDDB2E2E675C9088318D26694ACDD898F19550C9A45E5972AF657C06BDA1C78239353979E4365C65DAB0B71029F4219A3A213443947E06C6DF7C620EF008FE604ED866453BD6B16AC9ED036608EE9D3E4AB2ABA5E94CF25293D4B45AFA0D567996479CC285953236ADE45F840D1DD281F89E12F2A1C6E117B383BABF970BA0C521231D55D3530D022C39CA6C0084F7498C030D0616A64286881206350C6C0BA31E0E4B49916B5D61F4FE10D841F216CAAB877EE1F939533C57D2438B0BEFE9E162BAF031F82CFDB73DAC275673A9EBD02CCC5889E45D43228FBB8BEA1724884C30A87897E3AF5002A2AA2D2EA3141184CD4EE6920518F7E51839AAE639FC05B56F4F720AF3B398DDB3AFB315DD7356FD6177C5544CB1076552C9F3BB58DF24EC769C5C51B9DBA097B471965BCB1661C2AE344749CC218A088A8F3F1FE601362816A940F103E40F57DC1BC4FC0BEB7F99E7929DD59C462E6A235A3AD275F076E143843611BCD4E716C5774DCD3A829AE1BC86F2E2890661D8D4318CB68440A44350F240FD1C11A35DACB03F2636A0D24AF318D44B7D6006D023908443FCA41440E8AEA418BDD6D0CD59E7176D766D5D46C73DDA42794934365FAC235E71A2B5315B99AA875BA016A41EE3B12AC599E98EF27CED46A5AC9DA2106FDACA87C0EE1B3A07F0372CC553F5A839A4771020F54DCFBDD1ABF3C8E8BB469256BC71A755ACECFADDADD1B56DDC783E9FFBC61B8049A27216622CA1918CE1065A2C2A974FCDE7B0579D68A6E2E3CE06C8D35ABF2C3719136C57D2438A0A1619AC0B588FD342AFB54A936862A901AB5EC05AFD288A9C3730E35D152A7795E24D9466E2A7CD9FDC1601A82832D0CD1A81DEE10C40E4665B82085882D44A508A110A588049FDF31B0C00E81D7515E5791DF9C17AC79BB3B3F7ADA9AE714D70D0C8E149CA7463FA3E8444526083206F453F8DB5D1C814E0F510CEEA18E0DC06E85DD063E42F84895BF88D8B7EB9BEBDF7DE15FEEF475A3D85564A44F2B9EBF6150A37A611C19AEE808A30C5708280C12245BD03C84902A5DF1216485E8F045955A448F82396AD0C3D6D883812667F753FF32376337FB9CC4499CC4499CC4499C4457D061C811292BF81BC53C172B53F4F9FCD2EAEB33A756FF868F018C13001D122B114C32A3E04F24FAC392CE098793A4F5002749EB014E92D6039C24AD0748ABF9A8BA638275B44C30C60EB042AEA3B6D9B3818FF386063F48C7756AE9826E20ABB6263CC131669055426AAD27EA7C9C877E286E65A7B6C1C745DAC1154306051B73AF56D1CB81BF8DD03841A24B3E88824510E311391469AE5B5AF8BA20AB434B2AFF23319FDAF2C2AB4489F39D26B031545AB5A0331D6ACB0AB749DC9D05A2A15CEFECD8AB47F48151B9F575DE54B57239A217476A649241B3D446372B4404C41201AFAEACF00F20EB43132B1F49E634A5C7CD3352165E99D314AC52D19F10BD376012497F04CD42B858457F5A5B1EFEA96E88DF4C69CA6A78156434D1CDEAB1C05885D9AAA9D7FBD49D9C2DF0F9F874EAC512565B16BE23526BAB547902D179C0D9A0C9B6FF1CE07CD087EBDF0F3FBF6FE5703FCF103D274DA303DE644BCA49212A5F8BD414FD5BAC6C68C9C787417F91107564A43C7C41AABC6A9D83E348F8A18C10E75CDD20C3E2EF98EA1A14B922D418F8B15F58CF5F04CA0BADFF3501CF2BB24095AB800B800BACD82B40BE8DF2864FE2EF25BA5915111FB37999914A056365828F388E345579AD35AF66415E46B414B1D3DBF434864B55F9A620FFE5577E6D79D11712A53DEED322C1A697F31A035FABB74D3F1FE61E4CBA51A22E8F45A4702310EB5DD418E12BC0F676F53CBB458D2438FA9599C0E2A4791B26763053118923AD45CD2B59785FADD7861753EAA9FC38525EB41AF4E6B8ECA2DE9F5F8A95F5B8A68D98BFFF68A8B47A632A4500C4A5C558F93E8946CB2A9F897DCC75AB77016FC7A7D6B31ADC82A44639A29AD87C5B724326CEA9E62077F7C1D092AA273AD553D0906D9E4FFB26B5BF9E90A1715AD4E39EC679A41262DD14B6423B36516B4CAA267A51DC93F2DB56EFF23D82B8FB6A51E24E48AB68073D33781F81C4D9E45AF5B977C551BFE340BEDE9223EEC8511CB3D66C2BE5C51E2BD89683689C9EE2D385F5AB19416851D59B107FF78AC225BEF716887EBE83482423D723A56D46A02E810613FEBC62CE52743222C3501D841242C8559FEB8C122182D695B115F85E8CD89166B90AF8515C79D8CF27ACA11EC92DD8E3F3A6EEA0A7A90F147C4ED59C8DEA998829407580429E4008F07B23C7E1B849AB778B4EB5424944F43AA28675ADDAB5FEEDEE76B46A0522B1A421C24C124843989A90705BAAEB2A8FDE1B1EE1B498BB22D8AF63A5A85DB93663BFB65CBAA0E27135CF4879D1ADD6E8075117AA24F5BBDD1D8434F85F40A209E717D51DD3EEF0AEC11D359EC40BA1A57DDCD851CFB2A259A64576287A07B411D673F4B8A6D59685EF50D5FB13C407412A10FB06965D6A4CA3117B44AD78C01374A589BABB1AEACA0A9F07AE8D11E7479C86CB816701AC63A72654092B12F075455DBBB4F006451F23BE82D42AFA8C11794DB13B15A7C18857ABD63403EBE8E4305C8F483BEA862749D4A7452C9E6ACC6EF856744AD41191B2C286AE9E2E53B442906BE385329356D254756A4282DFF8DDB35EEB8E088BB09AD86B2EE1151CAF387FD15E5FDBB34859F8482753DE9E354F313287783BDC9A50AE9D9D8CB0EEC25AF32C1D2D8AAE84D61B1CA38E7C8FE923E27FE5B8C9FA26C439B96B48455857D123D20C7C31F65945B725BB05B62718E856EE57F84D82784C6459D1E8ECC6E00C88B32EB218F34457F44479E378096BCDB7FB50189520E885739D1D07BAD6728948DB4D40ED65FB364D008BC6DF0DDAA9ABC9AEA18BA4D9C4C2E2D289C899A952D7B9A38B5AAD17BB8C80753A9066D0692057C60BF527C9F288BAF08C7DE60CDD88932CFEC7EE98C1209DDE7CD681344F3C1FC3BA78CF53123D987A0CCA9991A5E1F81A40EBF5E14B8BAEC7B4FC81C473E79D20E8EEF950E0FDF86298457CD33C12C96EF15DF38AEA2989C70F87D7BF5F78AB5FDCDAF2C2ABB24DE35B0A133BD3CD6F5E15EA583871A775157D11E22F3A55318F47CA8A3EA7CAAFC03689918991657C13D173E831B4A2D5FD6B8C7A7185FE68C4FCFD49CF99AAE8CBA84C89D79D07EA96164E1478D11AEA44752CC8D7513A4CCB92A1434D13950EA4A9C45F76652CFF4AE2120A9AA5E81D883E8DC8B3AA3C80B6FBF7561192DE4D9C42BD54FE3CD4AA5D9B2AB5F5740351DBDE5808C21C157E26CA73206BE0186102496B6EBB561D0AF2A969A8C42DEBE4BAD5BB44984DF29BC36213EF5091AB72F39CEFD24D4700B9132B5F037CED6705D936C0AD7E2F55FA016ECD5EC4DED0353DD9232AB342A555B38194A70D3BBE08440A1345AA1DEFDA0C2DAE7AD2133957841FD171DA03F026E8BC901D36397F71E50BADEB5C4F105D057D291A9E1A721D1EC2AA9834ED1F2BDAA58BEBF316D73CD3AAE7E380CFB048DE51D13B42B9F6F4989DB24D6DE588F0EB0E2912057565E10A90E9B132C59C935FBAE7EDC4B8EDE12E813A53708660B22DDAA001A7BAAFAFFDF6836E20ABAE6AD41962BCACE3D1338EB40677D478CFD8F712B6B83E0A2DAE3A2DD9D5DF9F441C9B933D302AD7332D8F25EE09AAB0EE2461F188FA747547E4476ABD27412E4A08FF735E9EF36F3EE93ED130B56523CF8E38815781AB12C28E5874C6F16C54FCFF0A23E83294C469500D70F980D2EAED7E893EE9302DD6DC48DC75DFB2558C7C36AFB4AAD321C1271502505B5670163877A3F250FE923DFFD9D74A9DC4499C04C0FF0261F3AD9DD80D862B0000000049454E44AE426082
            ContentType: "image/png"
        ]
        SqliteLogo: @StaticBinary [
            Body: 0x474946383761DC006500F70000044A5C44A6D4A4BAC4447A8C0462840C92CCBCDEEC2C667C2C7A9C74BEE41C5A6C0486C4045674B4CED4E4EAEC2C96D464929C6CB6E45CA6D48CCEF48CAAB4046EA41C92CC148ACC14566C5C8694C4D2D4ECF6F4047EC4A4CADCD4DEE42C7694749EAC5CAEDC14526C44A2D404526C045E841C729494B6BC1C9AD4048ECC2C9ED46CB2D45486942C8EBC84C2E40C5E7C749AA42492CCFCFAFFDCEAEC0C7EAC046A9474B6E40C566CACD6EC0476ACCCDADC3C72840C4E64548294346E7C347A9474C6EC0C8ACCECF2F42462746C9AA494B2BC1C8EC45C8E9CF4F6F4B4C6CCDCE6E464AEDC5C9ECC044E6C047EB44C82940466941492D4CCDEE44496C47CBEE4245E74048AC4ECEEF46C92A494AEBC145E7CCCD6DC84CAEC7C9EAC0C52649CB6BC3CA2D444768C044E644CA6DCA4BEC44C7E8C04668C84AAB4346A7C2482AC1C5E74BCCED4E4EEEC64AACC8CD2F48CAEB44C8EAC6CC2ECCCE6F494CAE4ACC6CC3472840C76A4C4E2F4246E8CDCE2E4B4DAEC347EA44CA2DC0C82B4ACC2CC4C96BC7CCAEC249AD4349ED46CB2E4648E9C2C82AC5CAADC0472A4148EC4145A6C5C8A94C4D6DC0482C484C6ECDCEEF454AADC3C768424667CF4FAFCB4CACC5CA2C47CC2EC84CEF47CA2AC9CBAC42472942C6A7C2C9AD4D4E2E4FFFFFF9CCEEC1496D46C96A4BCD2D4044A64047ABC04628C0C92D484A6B44C92B41C5A740486CC04567C6492A4C4D2DCECF6FC44A2DC045274045E8C6CB2DC749AAC2492D454A6DC0C7EB40C56740476B4CCDAE4346E8424627C1C8ECCF4F6FCDCE6EC64AEE4047EBC7CBEEC048ACC045A7C349AD40C526C9CB6C43CA2DC2496D464B2E4A4BECC4C7E94E4EEF48CAEBC0C8ECC0472AC1C96D4148ECC145A745C8A9C0482CC74BAE43C768CB4CAD47CA2B40000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002C00000000DC0065000008FE003B091C48B0A0C18308132A5CC8B0A1C38710234A9C48B1A2C58B18336ADCC8B1A3C78F20438A1C49B2A4C9932853AA5CA99097140D8BAEB09C49B3A64724647A8802206AA7B22436830A1DBAD04306623B93F2C4808517D1A75069B2B9C6A367D2AB3C073888CA15A4240338C28A1D4BB6ACD9B368D38AEDA04C694F9E584581EA4A57A39E1041A67198C6B7AFDFBF80030B1E3C98D1B4514DDCBAE5095754A8BA9029DE9965D842A04D97316FD2CC39B3E7CD9F3B831E2DBA746623C2AEC68D0B8090D3C8B01B2E0B302D4880384000E5DEADBB37EFDFBE83031F2E5C3810046F172777ABA957ECE70A9131DA04C4CD04EBD8AF6BCFCE7DBBF7EEE0BFFE7BB7E486CBA9E57019A7B7CAD30FF4F70579E58D33C1527D4BF8F3EBDFCFBFBFFFFFFBD537012548ADA79A280754C5584FD7C0E760270670700C175C4CC0857D164E90E1861572A86187207E28A287245678E1046D90B0E0823B01C0431818A0274A0F0FC2E7C9348258D8888514F6E8E38F400629E49041AE70038B068A804518320210468DEFCD8163233D5249E4955866D923359158B51A4F14E8A4184F0740099D948234A2E69A6CB6E9E69B70C629271CC9AD071721548DB9532E663E8726206A022AE7A08416BA6602D134F9161AACA8C15E9DA2ECD0676C7370304C2395548269258064DA29A79E860AEAA89F962AAAA9A4869AC00FAC2527022BFE19E899540693C226E5309966DA0830BBF6CAEBAFBE060BECB0C2164B2C1348B5CA53117EDCA0EC4E73D50AD9ADC0546BEDB5D866ABEDB6DC6A4B8D1690AAD6C5158EAEB89C2802483BED349B009380BBF0BE2B6FBCF4CE6B6FBDF8DE2BEF790034B6DE0E5798A21A92A2F0B086BA7549D9AEBEF936CCF0C3F852030C25D1D899DC0EBC68808AAC70A19107C27449F9003536906C72C9289FAC72CA2CAFEC72CBD4C4F2C1A36E89009434AD62A5CC6B2047756BCB40BF2C74D0449B7C9EA2A26021C32239B3684ACF5D891C810D544F6D83D55857ADF5D55B67CDF5D75E63DDE56A3B89E080100710CC1A2B50733507230F5C1DC1DC74D76DF7DD78E7ADF7FEDC4C7869AE28A6C84081DAE6A22243DB3E4FF3C0208C37EEF8E390472EF9E48DFB82878C498571052EBA28E76F4F4A230ED5DB0F203388E911A03E4804ABB7CEFAEBAEC70EFBECB2BBFE07317FC3A5412720707C150F4089FE9494C72C81CCF1BE208F4CF2CC2FEF7CF3D03F2F7DF4CD239309CD0B6620431E3C7CDEAAE6C20FCFC1034BF8B244F9E89BAF7EFAECAFEF7EFBF0FB12822F12205D052B32C802E9DF3B3113FEF0D39845089610820116F080084CA00217C8C00412700FE1624C173AD12803B1060022E0D9FF82423C4384C08320FCA0084348C2119AB08428FCA02168012EB599AD135DF05D6328B041A248290686C8A10E77C8C31EFAF087FE4034C4140AB4BF06C960089E839408645243A14869168F88A214A748C52A5AF18A587CC4F52C268A301C8E025F3A972868D844274E6317B448E32368B1C636B2F18D6E8C231CE7284739D2C2169813857B1CC02482AD080055F04019CD68015A8CC190883CA42213C9C8453AB291903C642192D8032474A2087AB2201907691329ED820F6300A5284349CA519AB294A83CA52AF9C00722C6C50F87CB451ED7C3032672B2269E1C812B46A04B5EEEB297C0FCA5307D49CC60167397AE08C024B98886C399C28F4A81CB266F49132941C3156030C6088C01066C6A939BDEDC6637B329CE7082939CE7FC66378D61024515A3130E8891EF7672808F511397D3FE800637F799CD7EF2F39FFE0C2840070A06300CE305FCAB6727C028C39E4CF39E2C91D20504310C8A1AA3A2C33028450571518B6294A318D568453B3A0C920A62A32D48567A78A2B42B54C3407E5C2244F1098D939E7413271D860A2A7AD39CEED4A638A5E84F7B2A549EDA74186950942027A1D2085AE5A13355899482401ACE583533A6D9CC55B57A99AE6EA606D8EBE2E194913B4849C396515D894403C1D6B6BAF5AD70C50C5CE7CA56B95EE615CAE141BA1CD0BD30766C0B874B6B446B8382403C2310854D2C6217ABD8C43696B19075EC622D40B61E6C2586FB83947B043B93A93EE3B328F8AC68474BDAD29AF6B4A285C69844500C193860072C22FE9B1739DBD96904E313CFF8846E77CBDBDEFA16B7BFCD2D7087FB090B940052E04B861832BBA0300892B611654410782BDCEAEAD6BAC315EE75B7FB5BDD5E800676500C19098139B828230B79082C74A53A8D547C220ADD8DAF7CE30BDF28F48101B5484C7200DB892AC8AA0749482F4264C0095684620B68C5082F3C100A037B20C10C9141830DCC0A0A53781150929215A250000E7318111D0E31883D5C80118BF8C321FEB08A4B1C851A00C00CAA689128AA209024ACA60A5870002FF2E001491444087418801788A10635C46808D220C322D6908BE73A6469450843220A960835A0020362208632CEB0054B2E64193B189BB9D420A91A4929150548B3FE9A4BA1E634B3B9CD6F6EB39BE5BCE639334014AF38EE5BB0D0091960015258D8022BBAA08C5CC0408342388A2E4C3189456C610D98A8C6725151E42D3C041784D89817C270096B1C78C96400C594D5A08C2FA8F720AEF5C019A6BC20186C61061966442A9A41EB0234C3D6B8BEB5AE719D6B5BB399CDBA2EC5ADDFFCE62024E515FD2AD8C15891B69E886007844E8E26D0BA0549BBE6204CEE57157410610F1061276A200284078284132051147508DE42AEB1A032F5E9CC29484129E2DD0C7ADB3B05F5C6F7BDF3CDEF7DDF5B188DE9493D65708602F1E0CA8FC280933BC189018862006C48482F76200A35707B2127E8120050B1061F2F84153D586E34FEBEE0F184D89831849814BCE3CDF296BBFCE5308F39CB83E10467402AE59CE8E3A31823023A1464153C513742A4300435E042214288214F76C089874802933CB97642D6B0A006BDBBBD32CFBAD665FE0B023801D9C9C9422792503119AB461A4228887F01E01C85C8401ADB56C809620480265823224870784F8AE0E583B04229137CB7AC53108C6010FEF0864F3CE217AFF8C6339EF03968C22162BCA0A3B37B606FE1C1D109E280A48CBB20985083A511E2003530461AA776480350D1AF212423F503A1BA559E46114C80E29D1CE44BE1095FF8DEFBFEF7C00FBEF0C1FA0A282CC8709D38401279500483D87827A35788030E7071832C83493D29833D25FEF286B760001308F9BB0429628DA4C88283B216BEFAD7CFFE05C08227B0B80A8D7A8179B848A3EF03498634231C8AB41F240B62D02FA8900C15710574270A43A041B1971C562711B207003B1071F8645BEC578116180CBF50027091183CF1059DC00C302570FE5710749014BA200515D10B18607FB007119700787E971E81371114A00BC470680B71051218126F330D56900AA9708142E87B41B800A4101718B006BC202648820A424710E2B7133DD0820DD1030BE27316B1062200179AB07D0B9814B4371132E0010DF079032103301086202125ED150C40F88670188772388772E8866E380A77961E68E0001E80441623005428030A927D0BE7105BE025FEA240850F016657D17C05C10A0B02551E810B43C0672221250B900A99F8837618849ED889A0F889A2D8896F9889EF277F9240077DA51A14A0889DD0160BA2099BF510BDC31865661132C002C9412305F180A2E08822F182D1C2837CB1006EA889C89889CA988CCCB88CCE988CC1608C9A6873C93141D7002968508807E107839814AB807F0A917757210D19213070210206F17749217622E1004D0000AB301252226B40988974788FF8288799C8016EE11E61707C5DE08A037105A0B03FB9803F0C9107CDB6136A581102B01E09B6068D888B9B130AB0C70B6B005B721111CBC00A6B600D0DA0036628106C980AF4D88C28E98C0BF08C8CD0922ED992D360FE188C805770E10132803B3B713F32209003D10B8AB073A2A00044008E06B108E5B213B8771112A9146C03850BC28E11C10B8B9005D2000955800123D80942B0068AB0313D51052CC00219C00284D07403760661A0005EC00344060950791095226B72B900D3409776599778799774699231D9978601187BC1081C708A4149413C2102B270053C59106F37880B1209803875ABA84719810B2C328B02219170018C0E7105D2B05CC9C613680503077000BA1080A27003A5599AB98061062109D640716A900B1B334354C8867DC9177FB99BC5D85E32E997C029932F099C15901453C80C7B87048B691042804932420CD2B01507610DAE340919C10975428050FE981C9CD91042F00598E007A6B05C3BB10C9C27059CC0643BC1021EC009EEE90144D90902607A58A00333B008E4956C9A507D05D1838671972619A078F99B041A9CC0A99B7AA117D40800ADD5035EF09662E8014C58276A809903610DAB28064F48111ED08FBD389115518BA2306E13C7520CB105CB95720451903B318008C186BF999B053AA3063AA3057A0B34E90133B003DF981157E007CA8724D17030BDE82C7061A11CBA1C583810909814DD191164F016E669100E500D3D118F484756B9C09F6397143E80827049A33029A6355AA605CA0131460C9DB00861109F15810459C07A75C27105810B55901E4F3A119AF91685B8A7BF68117EE079073171FED0B2104590654F1AA5A22002440A97095AA0661AA964FA9BEF5726D690A7188109412A7F05D10BB2347E18A17F57919514C49D80CA18535A10556AA20A510608E29A04415EC470090A01A3927AAB930A9C4738009D3009DA981132C00A6B97143C209D02C10BAE0A1747901183F316BA60107E8AA90E910C826A103BCA13C3781048803B039060C5803B973092521298B85AAE936A73F1B87920E100BAA8140D281050979318918BC9717E8FB820D2DA10815A9E07C1473B81A508517E00C002E41664A2A009DA59ABE6BAB064CA01B700004F5372EB3AA13D20B19DD71823D9100E800649C1A8D00AA21411A8A279103BDA13D96A10CDAA349C4001CA80FE0107400102B610E3CAB0341BA3A3C0A0178104A49A103AB7036D37105FA014FE6311D4699C3BAB8ECB72AAFC6A10794071F0A810B5C8036AA00B030003B8B09C9DE09F355BB38C700BA2B0A111710292B88D0A520DC67AAC1CCB136570B61281050BD2A80411AD1641AD236B105290B6F68A107F16296BA0033BCB10339BAB826BAEB77003703B11A6402B0D91079AB013E053108B509B3C600A0AF810BD308884D0824D9AB415E19C0090AA04810BB9C013337810CF240A664B115A3BB8AC1BA9B790084D591197500D3F2B7D14270A10806A60C4139110BB10210305C913CAF0AB9D20B79D5BAD05E101A30B380A11A500A00B87CB9C6C2B1038F0A85BFEDBBA32790B167711A6800A483AA8699B9406E1B400C0AB0E487768107DE968AAC7CB133B48101EE0033B41AB09D1A13C7109AE58043466108E70BDD73B0A4677112FC802D35B94318295F59BB6A2800958DB096CF0A9D1E0BB0621B07BD71045F004951BAFE3A6BC3DD190C57B023221032B8820D13B10A1A70C08310CD8EBBFD91B09C4FB10CF440C1E6C1002230AA57B109CD3A26460B9D2C01315BA10CEBB13799B10425006E468109E0BBA3D0909AC1A7B55A00C4A20107FC613D5409432500C89E0BD08A107D0B0C25E0C9CBF70006629BBEBE8A692A0A884F0B7AA0A038922066F90B148C00C4824066150BBA8B6B74B61C7054190A240BF057179FE0000AB9CD747B9F03104B60AC4200BC6CA09CDF6BC45800B79C0098B500CCA87014510889E20982C5CAE7AE1C2DCCB18D039BD570003B893BA0D8104EA49C74A18B02C5015C4F006EF8B10C970A717530CBD70054280CB6C9007D6400103B05C10DA0957E0B4A2200DB12C103DDC133B000A3D70005E00031B401032508257A10668A009A8108000200B6A3C1038F00816B0C9B9CA012D39042FEC1097800AA6E00710900B18300097500C64700619400208620ADD8C1048600D65607A181006A6500464500CD790018E02965F90B19D8009721A5BA1194642F704438001898001168D010A40B5634C417DC513C4000925DC09D6300038E92FBA70C309D1FE0A72E0092E40052EFDD2301DD3323DD334EDD29A20C813C10A64E06332A0047E0002FDDC1389D00345C0090D4C1042A00345C002475227D140063A50B908A1D45950D5565DD54590D55AADD559A0D5B036103AB006A1B0060636D6A17060E02800CA70003CEA07053C906B8005D570009A30001050045220B1443100385D17FBBA1EDFBB5E21910121DD15A650D23B31047D2DD820410877F71C4850C35781318C4D12D7E081EFC1A25791015F5DD9204106EF1A1B576085E9C103D1E9D920A1012AFA1EBCB00A8E79B06F8DDA16E1014FF020BC80590B52067A2CDB16C10B4F70CE90E107B5F9164E28D5BC3D119730C3CF910C68C0226A000352F01A6343E8A6C7AD108B0003502203B8900122E016B9D0031080052C5007855DDDA28391583000B9A009A5890603B00AAB6CDEB7C40B4AC009EDA904F92CDFFABDDFFCBD120101003B
            ContentType: "image/gif"
        ]
        Text1: @StaticText [
            Body: "Hello, World!"
            ContentType: "text/plain"
        ]
        index: @Dynamic @Func -> @Function [
            args: [
                @name ref
                @name props
                @name params
                @name headers
                @name cookies
            ]
            body: @Block [
                @Return @Complex [
                    [
                        key: @Constant StaticHtml
                        val: @Complex [
                            [
                                key: @Constant ContentType
                                val: @Constant "text/html"
                            ]
                            [
                                key: @Constant Body
                                val: @Complex [
                                    [
                                        key: @Constant html
                                        val: @Complex [
                                            [
                                                key: @Constant 1
                                                val: @Constant @head [
                                                    @title "STYX Site Index"
                                                ]
                                            ]
                                            [
                                                key: @Constant 2
                                                val: @Complex [
                                                    [
                                                        key: @Constant body
                                                        val: @Complex [
                                                            [
                                                                key: @Constant 1
                                                                val: @Constant @h1 "STYX Site Index"
                                                            ]
                                                            [
                                                                key: @Constant 2
                                                                val: @ForEach [
                                                                    expr: @Yield @Complex [
                                                                        [
                                                                            key: @Constant p
                                                                            val: @Complex [
                                                                                [
                                                                                    key: @Constant 1
                                                                                    val: @Complex [
                                                                                        [
                                                                                            key: @Constant a
                                                                                            val: @Complex [
                                                                                                [
                                                                                                    key: @Constant 1
                                                                                                    val: @Complex [
                                                                                                        [
                                                                                                            key: @Constant "#href"
                                                                                                            val: @Variable v
                                                                                                        ]
                                                                                                    ]
                                                                                                ]
                                                                                                [
                                                                                                    key: @Constant 2
                                                                                                    val: @Variable v
                                                                                                ]
                                                                                            ]
                                                                                        ]
                                                                                    ]
                                                                                ]
                                                                                [
                                                                                    key: @Constant 2
                                                                                    val: @Constant " "
                                                                                ]
                                                                                [
                                                                                    key: @Constant 3
                                                                                    val: @Complex [
                                                                                        [
                                                                                            key: @Constant a
                                                                                            val: @Complex [
                                                                                                [
                                                                                                    key: @Constant 1
                                                                                                    val: @Complex [
                                                                                                        [
                                                                                                            key: @Constant "#href"
                                                                                                            val: @Concat [
                                                                                                                expr1: @Constant "../rest/urls/"
                                                                                                                expr2: @Variable v
                                                                                                            ]
                                                                                                        ]
                                                                                                    ]
                                                                                                ]
                                                                                                [
                                                                                                    key: @Constant 2
                                                                                                    val: @Constant "[Source]"
                                                                                                ]
                                                                                            ]
                                                                                        ]
                                                                                    ]
                                                                                ]
                                                                            ]
                                                                        ]
                                                                    ]
                                                                    ident1: @name v
                                                                    init: @Call [
                                                                        args: [
                                                                            @Constant [/urls]
                                                                        ]
                                                                        func: @Constant -> @CompiledFunction session_browse
                                                                    ]
                                                                ]
                                                            ]
                                                        ]
                                                    ]
                                                ]
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
    ]
    users: [
        philip: [
            birth: "1977-10-26"
            gender: M
            name: "Philip Oswald"
        ]
        [/]: [ ]
    ]
]