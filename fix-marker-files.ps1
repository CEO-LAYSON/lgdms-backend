# Fix marker files with corrupted package declarations
$basePath = "src\main\java\com\crn\lgdms"

# Define the files and their correct packages
$files = @(
    @{path="src\main\java\com\crn\lgdms\modules\inventory\dto\dto.java"; package="com.crn.lgdms.modules.inventory.dto"},
    @{path="src\main\java\com\crn\lgdms\common\mapping\mapping.java"; package="com.crn.lgdms.common.mapping"},
    @{path="src\main\java\com\crn\lgdms\common\security\auth\auth.java"; package="com.crn.lgdms.common.security.auth"},
    @{path="src\main\java\com\crn\lgdms\modules\receiving\dto\dto.java"; package="com.crn.lgdms.modules.receiving.dto"},
    @{path="src\main\java\com\crn\lgdms\common\security\security.java"; package="com.crn.lgdms.common.security"},
    @{path="src\main\java\com\crn\lgdms\modules\users\dto\dto.java"; package="com.crn.lgdms.modules.users.dto"},
    @{path="src\main\java\com\crn\lgdms\common\validation\validators\validators.java"; package="com.crn.lgdms.common.validation.validators"},
    @{path="src\main\java\com\crn\lgdms\common\web\web.java"; package="com.crn.lgdms.common.web"},
    @{path="src\main\java\com\crn\lgdms\common\security\rbac\rbac.java"; package="com.crn.lgdms.common.security.rbac"},
    @{path="src\main\java\com\crn\lgdms\common\api\api.java"; package="com.crn.lgdms.common.api"},
    @{path="src\main\java\com\crn\lgdms\common\exception\exception.java"; package="com.crn.lgdms.common.exception"},
    @{path="src\main\java\com\crn\lgdms\modules\sales\repository\repository.java"; package="com.crn.lgdms.modules.sales.repository"},
    @{path="src\main\java\com\crn\lgdsecurity\auth\dto\dto.java"; package="comms\common\.crn.lgdms.common.security.auth.dto"},
    @{path="src\main\java\com\crn\lgdms\modules\locations\dto\dto.java"; package="com.crn.lgdms.modules.locations.dto"},
    @{path="src\main\java\com\crn\lgdms\common\validation\annotations\annotations.java"; package="com.crn.lgdms.common.validation.annotations"},
    @{path="src\main\java\com\crn\lgdms\common\validation\validation.java"; package="com.crn.lgdms.common.validation"},
    @{path="src\main\java\com\crn\lgdms\modules\users\domain\entity\entity.java"; package="com.crn.lgdms.modules.users.domain.entity"},
    @{path="src\main\java\com\crn\lgdms\common\enums\enums.java"; package="com.crn.lgdms.common.enums"},
    @{path="src\main\java\com\crn\lgdms\modules\payments\dto\dto.java"; package="com.crn.lgdms.modules.payments.dto"},
    @{path="src\main\java\com\crn\lgdms\common\constants\constants.java"; package="com.crn.lgdms.common.constants"},
    @{path="src\main\java\com\crn\lgdms\common\security\userdetails\userdetails.java"; package="com.crn.lgdms.common.security.userdetails"},
    @{path="src\main\java\com\crn\lgdms\common\util\util.java"; package="com.crn.lgdms.common.util"},
    @{path="src\main\java\com\crn\lgdms\common\security\Jwt\Jwt.java"; package="com.crn.lgdms.common.security.Jwt"},
    @{path="src\main\java\com\crn\lgdms\modules\notifications\dto\dto.java"; package="com.crn.lgdms.modules.notifications.dto"},
    @{path="src\main\java\com\crn\lgdms\common\pagination\pagination.java"; package="com.crn.lgdms.common.pagination"},
    @{path="src\main\java\com\crn\lgdms\modules\transfer\web\web.java"; package="com.crn.lgdms.modules.transfer.web"},
    @{path="src\main\java\com\crn\lgdms\modules\reports\dto\dto.java"; package="com.crn.lgdms.modules.reports.dto"},
    @{path="src\main\java\com\crn\lgdms\modules\masterdata\dto\dto.java"; package="com.crn.lgdms.modules.masterdata.dto"}
)

foreach ($file in $files) {
    $content = "package " + $file.package + ";"
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file.path, $content, $utf8NoBom)
    Write-Host "Fixed: $($file.path) -> $($file.package)"
}

Write-Host "Done! Fixed $($files.Count) files."
