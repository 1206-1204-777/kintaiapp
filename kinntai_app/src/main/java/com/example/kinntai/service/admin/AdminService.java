package com.example.kinntai.service.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.admin.EmployeeCreateRequest;
import com.example.kinntai.dto.admin.EmployeeDto;
import com.example.kinntai.dto.admin.EmployeeUpdateRequest;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.LocationRepository; // LocationRepositoryが必要
import com.example.kinntai.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository; // LocationRepositoryを注入
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(UserRepository userRepository, LocationRepository locationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 全従業員の一覧を取得する
     * @return EmployeeDtoのリスト
     */
    public List<EmployeeDto> getAllEmployees() {
        return userRepository.findAll().stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 新しい従業員を作成する
     * @param request 作成リクエストデータ
     * @return 作成された従業員のDTO
     */
    @Transactional
    public EmployeeDto createEmployee(EmployeeCreateRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent() || userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Username or email already exists.");
        }

        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid location ID: " + request.locationId()));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); // パスワードをハッシュ化
        user.setRole(request.role());
        user.setLocation(location);
        user.setDefaultStartTime(request.defaultStartTime());
        user.setDefaultEndTime(request.defaultEndTime());
        // user.setHireDate(request.hireDate()); // Userエンティティにフィールドを追加した場合

        User savedUser = userRepository.save(user);
        return EmployeeDto.fromEntity(savedUser);
    }

    /**
     * 従業員情報を更新する
     * @param userId 更新対象の従業員ID
     * @param request 更新リクエストデータ
     * @return 更新後の従業員DTO
     */
	@Transactional
    public EmployeeDto updateEmployee(Long userId, EmployeeUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid location ID: " + request.locationId()));

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setLocation(location);
        user.setDefaultStartTime(request.defaultStartTime());
        user.setDefaultEndTime(request.defaultEndTime());
        // user.setHireDate(request.hireDate());

        User updatedUser = userRepository.save(user);
        return EmployeeDto.fromEntity(updatedUser);
    }

    /**
     * 従業員を削除する
     * @param userId 削除対象の従業員ID
     */
	@Transactional
    public void deleteEmployee(Long userId) {
        // 関連データの削除処理などが必要な場合はここに追加
        // 例: attendanceRepository.deleteByUser_Id(userId);
        userRepository.deleteById(userId);
    }

    // 他にも、休暇申請の取得・承認・却下などのメソッドをここに追加します。
}
