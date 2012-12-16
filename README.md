# S2Test

Seasar2でJUnit4.10以降を使用するためのライブラリです。S2JUnit4がJUnit4.10でうまく動いてくれなかったので再実装しました。

## 機能

S2Testを使用することでJUnitのテストにて以下の機能を使うことができます。

* テストクラスへのDI
* インスタンス、コンテナに対する操作を行うためのRule
* テストインスタンス生成タイミングで操作するための拡張Rule
* MockServletContext等のSeasar2のMockサポート

## 使い方

### テストクラスへのDI

@RunWithにてS2Junit4ClassRunnerを指定します。

    @RunWith(S2JUnit4ClassRunner.class)
    public static class Sample {

        public DummyLogic dummyLogic;

        @Test
        public void dummyLogicにインジェクションが行われてる() {
            assertThat(dummyLogic.dummyService, is(notNullValue()));
            String actual = dummyLogic.execute();
            assertThat(actual, is("example1"));
        }
    }

DummyLogicにインジェクションが行われます。

#### Diconファイルの指定

RootとなるDiconファイルはクラスに対するアノテーションで指定できます。

* デフォルト
  付与していない場合は同一パッケージに格納された「クラス名.dicon」を読み込みます。
* @RootDicon( path="hoge/hoge.dicon" )
  pathに指定されたdiconファイルを読み込みます。
* @RootDicon()
  pathが指定されていない場合、空のcontainerを生成します。

#### テスト内でのコンテナの使用

    @RunWith(S2JUnit4ClassRunner.class)
    public static class Sample {
        public S2Container container;
    }

テストクラス内にS2Containerのフィールドを作ることで自動的にDIされます。

#### コンテナ初期化のタイミング

デフォルトではコンテナはテスト全体で使い回されます。
DirtyContainerRuleを使用することでコンテナ初期化のタイミングを操作することができます。

* 以下のルールを設定することで、メソッド終了時にコンテナを初期化します。

        @Rule
        public TestRule dirtyContainerRule = new DirtyContainerRule();

* 以下のルールを設定することで、特定のメソッド終了時にコンテナを初期化すます。

        @Rule
        public TestRule dirtyContainerRule = new DirtyContainerRule(Mode.DIRTY);

        @Test
        @Dirty
        public void test1() {
                logger.debug("test1()");
        }

   初期化を行いたいメソッドに対して@Dirtyを付与すると、そのメソッドの**終了時に**初期化が行われます。

### インスタンス、コンテナに対する操作を行うためのRule

JUnit4.10ではMethodRuleは非推奨となり、代わりにTestRuleが追加されました。しかし、TestRuleはテスト対象のインスタンスを受け取る事ができないため、Seasar2でのテスト拡張を行うためには少し不便です。
そこで対象のテストインスタンスと、現在使用しているコンテナ等の拡張情報を受け取れるRuleのベース**S2TestRule**を用意しました。
DirtyContainerRuleもこのRuleにて作成しています。

S2TestRuleを拡張することで上記情報を使用したRuleを作成することができます。
S2TestRuleは@ClassRuleにも対応していますが、@ClassRuleの時は当然インスタンスの取得は行えません。

### テストインスタンス生成タイミングで操作するための拡張Rule

JUnit4.7で追加された@Ruleは@Beforeと@Afterに対する操作を共通化することができます。そしてJUnit4.10で追加された@ClassRuleでは@BeforeClassや@AfterClassに対する操作を共通化することができます。
しかし、DIを行うタイミングとしては@BeforeClassや@Beforeのタイミングではなく、@Beforeの前、テストインスタンスを生成するタイミングで行う必要があります。
そこでS2Testでは@InstanceRuleを追加し、インスタンス生成時のRuleを作成できるようにしました。@InstanceRuleを指定することでテストインスタンス作成時の処理を拡張できます。

@InstanceRuleを使用するためにはS2IncetanceRuleを拡張している必要があります。インスタンス生成時に指定したRuleがすべて適用されます。
S2JUnit4ClassRunnerによるDIも実体はS2InstanceRuleを拡張した**DependencyInjectionRule**にて実現しています。


    @RunWith(S2JUnit4ClassRunner.class)
    @RootDicon()
    public static class 各ルールが設定されている場合 {
        public static class S2PrepareInstanceRuleSapmle extends S2InstanceRule {
            @Override
            public void apply(TestContext testContext) throws Exception {
                logger.debug("S2PrepareInstanceRuleSapmle.apply()");
            }
        }

        public static class S2MethodRuleSample extends S2TestRule {
            @Override
            protected void before(Description description, TestContext testContext)
                    throws Throwable {
                logger.debug("S2MethodRuleSample.before()");
            }

            @Override
            protected void after(Description description, TestContext testContext) throws Throwable {
                logger.debug("S2MethodRuleSample.after()");
            }
        }

        public static class S2ClassRuleSample extends S2TestRule {
            @Override
            protected void before(Description description, TestContext testContext)
                    throws Throwable {
                logger.debug("S2ClassRuleSample.before()");
            }

            @Override
            protected void after(Description description, TestContext testContext) throws Throwable {
                logger.debug("S2ClassRuleSample.after()");
            }
        }

        @ClassRule
        public static TestRule s2ClassRule = new S2ClassRuleSample();

        @Rule
        public TestRule s2MethodRule = new S2MethodRuleSample();

        @InstanceRule
        public S2InstanceRule s2InstanceRule = new S2PrepareInstanceRuleSapmle();

        @BeforeClass
        public static void beforeClass() throws Exception {
            logger.debug("beforeClass()");
        }

        @AfterClass
        public static void afterClass() throws Exception {
            logger.debug("afterClass()");
        }

        @Before
        public void before() throws Exception {
            logger.debug("before()");
        }

        @After
        public void after() throws Exception {
            logger.debug("after()");
        }

        @Test
        public void test1() throws Exception {
            logger.debug("test1()");
        }

        @Test
        public void test2() throws Exception {
            logger.debug("test2()");
        }
    }

上記を実行すると以下のようになります。

    beforeClass()
    S2PrepareInstanceRuleSapmle.apply()
    S2MethodRuleSample.before()
    before()
    test1()
    after()
    S2MethodRuleSample.after()
    S2PrepareInstanceRuleSapmle.apply()
    S2MethodRuleSample.before()
    before()
    test2()
    after()
    S2MethodRuleSample.after()
    afterClass()
    S2ClassRuleSample.after()

### MockServletContext等のSeasar2のMockサポート

S2TestではデフォルトでHttpServlet等のモックを設定できる仕組みとなっていたため、その機能を引き継いでいます。
diconファイルを用意しないでも@ContextConfigurationにincludeしたい定義を指定することでコンテナにMockを指定することができます。

    @RunWith(S2JUnit4ClassRunner.class)
    @RootDicon()
    @ContextConfiguration(includes = { HttpServletTestInclude.class })
    public static class ContextIncludeが設定されている場合 {
        public S2Container container;

        protected MockServletContext servletContext;

        protected Servlet servlet;

        protected MockServletConfig servletConfig;

        protected MockHttpServletRequest request;

        protected MockHttpServletResponse response;

        @Test
        public void test() {
            assertThat(container, is(notNullValue()));
            assertThat(servletContext, is(notNullValue()));
            assertThat(servlet, is(notNullValue()));
            assertThat(servletConfig, is(notNullValue()));
            assertThat(request, is(notNullValue()));
            assertThat(response, is(notNullValue()));
        }
    }

includeに指定できるクラスはContainerIncludeを実装している必要があります。
これにより共通的なincludeを用意しておくことができます。

## TODO
* トランザクション関連のRule
* テストケースをちゃんと書く

